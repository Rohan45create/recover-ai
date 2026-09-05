package com.recoverai.service;

import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.CaseState;
import com.recoverai.domain.recovery.RecoveryCase;
import com.recoverai.repository.AuditEventRepository;
import com.recoverai.repository.PaymentRepository;
import com.recoverai.repository.RecoveryCaseRepository;
import com.recoverai.ai.AiDiagnosisResponse;
import com.recoverai.policy.PolicyEngine;
import com.recoverai.policy.PolicyEvaluationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoveryCaseProcessor {

    private final RecoveryCaseRepository recoveryCaseRepository;
    private final PaymentRepository paymentRepository;
    private final AuditEventRepository auditEventRepository;
    private final AuditService auditService;
    private final DiagnosisService diagnosisService;
    private final PolicyEngine policyEngine;
    private final DecisionService decisionService;
    private final ToolExecutionService toolExecutionService;

    @Transactional
    public boolean processNextCase() {
        Optional<RecoveryCase> caseOpt = recoveryCaseRepository.findNextAvailableCase();
        if (caseOpt.isEmpty()) {
            return false;
        }

        RecoveryCase rc = caseOpt.get();

        // STAGE 5: log case pickup
        log.info("[PROCESSOR-STAGE-5] Picked up case id={} currentStatus={}", rc.getId(), rc.getStatus());

        // Transition to DIAGNOSING and persist immediately so we don't re-pick this case
        // if the diagnosis call throws an unchecked exception
        rc.setStatus(CaseState.DIAGNOSING);
        rc.setUpdatedAt(OffsetDateTime.now());
        recoveryCaseRepository.save(rc);
        log.info("[PROCESSOR-STAGE-5] Case id={} status: ELIGIBLE -> DIAGNOSING (persisted)", rc.getId());

        Payment payment = paymentRepository.findById(rc.getPaymentId()).orElse(null);
        if (payment == null) {
            rc.setStatus(CaseState.STOPPED);
            log.info("[PROCESSOR-STAGE-5] Case id={} status: DIAGNOSING -> STOPPED (payment not found)", rc.getId());
            rc.setUpdatedAt(OffsetDateTime.now());
            auditService.logEvent(rc.getId(), "PAYMENT_NOT_FOUND", Map.of("payment_id", rc.getPaymentId()));
            recoveryCaseRepository.save(rc);
            return true;
        }

        // Log every field that the PromptBuilder / AI might need, so null fields are visible
        log.info("[PROCESSOR-STAGE-5] Payment fields: id={} amount={} currency={} method={} errorCode={} customerId={}",
                payment.getId(), payment.getAmount(), payment.getCurrency(),
                payment.getMethod(), payment.getErrorCode(), payment.getCustomerId());

        AiDiagnosisResponse aiResponse;
        try {
            aiResponse = diagnosisService.diagnose(payment, rc);
        } catch (AiServiceUnavailableException e) {
            // Transient AI outage — schedule retry
            rc.setStatus(CaseState.ELIGIBLE); // back to ELIGIBLE so it's picked up again
            rc.setNextRunAt(OffsetDateTime.now().plusMinutes(15));
            rc.setUpdatedAt(OffsetDateTime.now());
            auditService.logEvent(rc.getId(), "AI_UNAVAILABLE", Map.of("reason", e.getMessage() != null ? e.getMessage() : "unknown"));
            recoveryCaseRepository.save(rc);
            log.warn("[PROCESSOR-STAGE-5] Case id={} AI unavailable, scheduled retry in 15min", rc.getId());
            return true;
        } catch (Exception e) {
            // Any other failure (parse error, network, etc.) — stop this case and log fully
            log.error("[PROCESSOR-STAGE-5] Case id={} DIAGNOSIS FAILED with unexpected exception — stopping case", rc.getId(), e);
            rc.setStatus(CaseState.STOPPED);
            rc.setUpdatedAt(OffsetDateTime.now());
            auditService.logEvent(rc.getId(), "DIAGNOSIS_ERROR", Map.of(
                "error", e.getClass().getSimpleName(),
                "message", e.getMessage() != null ? e.getMessage() : "null"
            ));
            recoveryCaseRepository.save(rc);
            return true;
        }

        rc.setDiagnosis(aiResponse.getDiagnosis());
        rc.setStatus(CaseState.ACTION_PENDING);
        rc.setUpdatedAt(OffsetDateTime.now());
        auditService.logEvent(rc.getId(), "AI_DIAGNOSIS", Map.of(
            "diagnosis", rc.getDiagnosis() != null ? rc.getDiagnosis() : "UNKNOWN",
            "confidence", 0.89,
            "candidate_actions", aiResponse.getCandidateActions() != null ? aiResponse.getCandidateActions() : List.of(),
            "rationale", aiResponse.getRationale() != null ? aiResponse.getRationale() : "No rationale provided",
            "ai_provider", aiResponse.getAiProvider() != null ? aiResponse.getAiProvider() : "UNKNOWN"
        ));
        log.info("[PROCESSOR-STAGE-5] Case id={} diagnosis={} provider={} candidates={}",
                rc.getId(), rc.getDiagnosis(), aiResponse.getAiProvider(), aiResponse.getCandidateActions());

        // Phase 4: Policy Engine
        List<AuditEvent> history = auditEventRepository.findByCaseIdOrderBySequenceNoAsc(rc.getId());
        PolicyEvaluationResult policyResult = policyEngine.filterPermittedActions(rc, payment, aiResponse.getCandidateActions(), history);
        List<String> permittedActions = policyResult.getPermittedActions();

        auditService.logEvent(rc.getId(), "POLICY_EVALUATION", Map.of(
            "evaluations", policyResult.getEvaluations() != null ? policyResult.getEvaluations() : List.of()
        ));
        log.info("[PROCESSOR-STAGE-5] Case id={} policy result: permittedActions={}", rc.getId(), permittedActions);

        if (permittedActions == null || permittedActions.isEmpty()) {
            if (policyResult.isEscalationRequired()) {
                // Amount exceeds POL-04 escalation threshold — route to human queue
                String escalationReason = policyResult.getEvaluations().stream()
                        .filter(e -> !e.isAllowed())
                        .findFirst()
                        .map(PolicyEvaluationResult.ActionEvaluation::getReason)
                        .orElse("Payment amount exceeds escalation threshold");
                rc.setStatus(CaseState.ESCALATED);
                rc.setUpdatedAt(OffsetDateTime.now());
                auditService.logEvent(rc.getId(), "OUTCOME", Map.of(
                    "status", "ESCALATED",
                    "reason", escalationReason
                ));
                recoveryCaseRepository.save(rc);
                log.info("[PROCESSOR-STAGE-5] Case id={} -> ESCALATED: {}", rc.getId(), escalationReason);
            } else {
                rc.setStatus(CaseState.STOPPED);
                rc.setUpdatedAt(OffsetDateTime.now());
                auditService.logEvent(rc.getId(), "OUTCOME", Map.of("status", "STOPPED", "reason", "No permitted actions available"));
                recoveryCaseRepository.save(rc);
                log.info("[PROCESSOR-STAGE-5] Case id={} -> STOPPED (no permitted actions)", rc.getId());
            }
            return true;
        }

        // Phase 5: Decision Engine
        DecisionResult decision = decisionService.selectBestAction(rc, payment, permittedActions);

        if (decision == null || "NONE".equals(decision.getChosenAction())) {
            rc.setStatus(CaseState.STOPPED);
            rc.setUpdatedAt(OffsetDateTime.now());
            auditService.logEvent(rc.getId(), "OUTCOME", Map.of("status", "STOPPED", "reason", "No profitable actions available"));
            recoveryCaseRepository.save(rc);
            log.info("[PROCESSOR-STAGE-5] Case id={} -> STOPPED (no profitable action)", rc.getId());
            return true;
        }

        rc.setChosenAction(decision.getChosenAction());
        rc.setExpectedRecoveryValue(decision.getExpectedRecoveryValue());
        rc.setStatus(CaseState.ACTION_EXECUTING);
        rc.setUpdatedAt(OffsetDateTime.now());

        auditService.logEvent(rc.getId(), "DECISION", Map.of(
                "chosen_action", rc.getChosenAction() != null ? rc.getChosenAction() : "UNKNOWN",
                "expected_value", rc.getExpectedRecoveryValue() != null ? rc.getExpectedRecoveryValue() : BigDecimal.ZERO,
                "policy_version", "v1",
                "computation", Map.of(
                        "probability", decision.getProbabilityOfRecovery() != null ? decision.getProbabilityOfRecovery() : BigDecimal.ZERO,
                        "amount", decision.getBaseAmount() != null ? decision.getBaseAmount() : BigDecimal.ZERO,
                        "cost", decision.getInterventionCost() != null ? decision.getInterventionCost() : BigDecimal.ZERO,
                        "penalty", decision.getFrictionPenalty() != null ? decision.getFrictionPenalty() : BigDecimal.ZERO
                )
        ));
        log.info("[PROCESSOR-STAGE-5] Case id={} decision={} EV={}", rc.getId(), rc.getChosenAction(), rc.getExpectedRecoveryValue());

        // Phase 6: Execute via ToolExecutionService
        try {
            toolExecutionService.execute(rc, payment);
        } catch (Exception e) {
            log.error("[PROCESSOR-STAGE-5] Case id={} TOOL EXECUTION FAILED", rc.getId(), e);
            rc.setStatus(CaseState.STOPPED);
            rc.setUpdatedAt(OffsetDateTime.now());
            auditService.logEvent(rc.getId(), "OUTCOME", Map.of(
                "status", "STOPPED",
                "reason", "Tool execution failed: " + e.getMessage()
            ));
        }

        recoveryCaseRepository.save(rc);
        log.info("[PROCESSOR-STAGE-5] Case id={} final status={}", rc.getId(), rc.getStatus());
        return true;
    }
}
