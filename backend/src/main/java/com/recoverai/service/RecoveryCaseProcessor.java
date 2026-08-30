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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        // Transition 1: DIAGNOSING
        rc.setStatus(CaseState.DIAGNOSING);

        Payment payment = paymentRepository.findById(rc.getPaymentId()).orElse(null);
        if (payment == null) {
            rc.setStatus(CaseState.STOPPED);
            rc.setUpdatedAt(OffsetDateTime.now());
            auditService.logEvent(rc.getId(), "PAYMENT_NOT_FOUND", Map.of("payment_id", rc.getPaymentId()));
            recoveryCaseRepository.save(rc);
            return true;
        }

        AiDiagnosisResponse aiResponse;
        try {
            aiResponse = diagnosisService.diagnose(payment, rc);
        } catch (AiServiceUnavailableException e) {
            rc.setStatus(CaseState.DIAGNOSING);
            rc.setNextRunAt(OffsetDateTime.now().plusMinutes(15));
            rc.setUpdatedAt(OffsetDateTime.now());
            auditService.logEvent(rc.getId(), "AI_UNAVAILABLE", Map.of("reason", e.getMessage()));
            recoveryCaseRepository.save(rc);
            return true;
        }

        rc.setDiagnosis(aiResponse.getDiagnosis());
        rc.setStatus(CaseState.ACTION_PENDING);
        auditService.logEvent(rc.getId(), "AI_DIAGNOSIS", Map.of(
            "diagnosis", rc.getDiagnosis() != null ? rc.getDiagnosis() : "UNKNOWN",
            "confidence", 0.89,
            "candidate_actions", aiResponse.getCandidateActions() != null ? aiResponse.getCandidateActions() : List.of(),
            "rationale", aiResponse.getRationale() != null ? aiResponse.getRationale() : "No rationale provided"
        ));

        // Phase 4: Policy Engine
        List<AuditEvent> history = auditEventRepository.findByCaseIdOrderBySequenceNoAsc(rc.getId());
        PolicyEvaluationResult policyResult = policyEngine.filterPermittedActions(rc, payment, aiResponse.getCandidateActions(), history);
        List<String> permittedActions = policyResult.getPermittedActions();
        
        auditService.logEvent(rc.getId(), "POLICY_EVALUATION", Map.of(
            "evaluations", policyResult.getEvaluations() != null ? policyResult.getEvaluations() : List.of()
        ));
        
        if (permittedActions == null || permittedActions.isEmpty()) {
            rc.setStatus(CaseState.STOPPED);
            rc.setUpdatedAt(OffsetDateTime.now());
            auditService.logEvent(rc.getId(), "OUTCOME", Map.of("status", "STOPPED", "reason", "No permitted actions available"));
            recoveryCaseRepository.save(rc);
            return true;
        }

        // Phase 5: Decision Engine
        DecisionResult decision = decisionService.selectBestAction(rc, payment, permittedActions);
        
        if (decision == null || "NONE".equals(decision.getChosenAction())) {
            rc.setStatus(CaseState.STOPPED);
            rc.setUpdatedAt(OffsetDateTime.now());
            auditService.logEvent(rc.getId(), "OUTCOME", Map.of("status", "STOPPED", "reason", "No profitable actions available"));
            recoveryCaseRepository.save(rc);
            return true;
        }

        rc.setChosenAction(decision.getChosenAction());
        rc.setExpectedValue(decision.getExpectedValue());
        rc.setStatus(CaseState.ACTION_EXECUTING);
        
        auditService.logEvent(rc.getId(), "DECISION", Map.of(
                "chosen_action", rc.getChosenAction() != null ? rc.getChosenAction() : "UNKNOWN",
                "expected_value", rc.getExpectedValue() != null ? rc.getExpectedValue() : BigDecimal.ZERO,
                "policy_version", "v1"
        ));

        // Phase 6: Execute via ToolExecutionService
        toolExecutionService.execute(rc, payment);
        recoveryCaseRepository.save(rc);
        return true;
    }
}
