package com.recoverai.service;

import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.CaseState;
import com.recoverai.domain.recovery.RecoveryCase;
import com.recoverai.integration.EscalationService;
import com.recoverai.integration.StopRecoveryHandler;
import com.recoverai.integration.notification.NotificationSender;
import com.recoverai.integration.razorpay.PaymentLinkService;
import com.recoverai.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolExecutionService {

    private final PaymentLinkService paymentLinkService;
    private final NotificationSender notificationSender;
    private final EscalationService escalationService;
    private final StopRecoveryHandler stopRecoveryHandler;
    private final AuditService auditService;
    private final AuditEventRepository auditEventRepository;

    public void execute(RecoveryCase rc, Payment payment) {
        String action = rc.getChosenAction();
        int attemptNumber = getAttemptNumber(rc.getId(), action);
        String idempotencyKey = String.format("%s-%s-%d", rc.getId(), action, attemptNumber);

        try {
            switch (action) {
                case "RETRY":
                    paymentLinkService.createPaymentLink(payment, idempotencyKey);
                    break;
                case "SEND_SMS":
                case "SEND_WHATSAPP":
                case "SEND_EMAIL":
                    notificationSender.send(action, payment.getCustomerId(), idempotencyKey);
                    break;
                case "ESCALATE":
                    escalationService.escalate(rc.getId().toString(), idempotencyKey);
                    rc.setStatus(CaseState.ESCALATED);
                    break;
                case "STOP":
                    stopRecoveryHandler.stop(rc.getId().toString(), idempotencyKey);
                    rc.setStatus(CaseState.STOPPED);
                    break;
                default:
                    log.warn("Unknown action '{}', stopping recovery.", action);
                    rc.setStatus(CaseState.STOPPED);
                    break;
            }

            auditService.logEvent(rc.getId(), "TOOL_EXECUTION", Map.of(
                    "action", action,
                    "idempotency_key", idempotencyKey,
                    "result", "Success"
            ));

            if (rc.getStatus() == CaseState.ACTION_EXECUTING) {
                // If it wasn't transitioned to ESCALATED/STOPPED, go to WAITING
                rc.setStatus(CaseState.WAITING);
                rc.setNextRunAt(OffsetDateTime.now().plusDays(1)); // Arbitrary wait time
            } else if (rc.getStatus() == CaseState.ESCALATED || rc.getStatus() == CaseState.STOPPED) {
                auditService.logEvent(rc.getId(), "OUTCOME", Map.of(
                    "status", rc.getStatus().name(),
                    "reason", "Action resulted in terminal state"
                ));
            }

        } catch (Exception e) {
            log.error("Failed to execute tool for case {}: {}", rc.getId(), e.getMessage());
            
            auditService.logEvent(rc.getId(), "TOOL_EXECUTION", Map.of(
                    "action", action,
                    "idempotency_key", idempotencyKey,
                    "result", "Failed: " + e.getMessage()
            ));

            // Backoff and retry from DIAGNOSING
            rc.setStatus(CaseState.DIAGNOSING);
            rc.setNextRunAt(OffsetDateTime.now().plusMinutes(15));
        }
    }

    private int getAttemptNumber(UUID caseId, String action) {
        List<AuditEvent> history = auditEventRepository.findByCaseIdOrderBySequenceNoAsc(caseId);
        long pastExecutions = history.stream()
                .filter(e -> "TOOL_EXECUTION".equals(e.getEventType()))
                .filter(e -> e.getPayload() != null && e.getPayload().contains("\"action\":\"" + action + "\""))
                .count();
        return (int) pastExecutions + 1;
    }
}
