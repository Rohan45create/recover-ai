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

        log.info("[TOOL-EXEC] Case id={} executing action={} idempotencyKey={}", rc.getId(), action, idempotencyKey);

        try {
            switch (action) {
                case "CREATE_PAYMENT_LINK":
                case "RETRY":
                    // Both these actions create a new payment link for the customer to complete payment
                    String url;
                    try {
                        url = paymentLinkService.createPaymentLink(rc, payment, idempotencyKey);
                    } catch (Exception linkEx) {
                        log.error("[TOOL-EXEC] PAYMENT LINK CREATION FAILED for case={} action={} : {}",
                                rc.getId(), action, linkEx.getMessage(), linkEx);
                        auditService.logEvent(rc.getId(), "TOOL_EXECUTION", Map.of(
                                "action", action,
                                "idempotency_key", idempotencyKey,
                                "result", "FAILED: " + linkEx.getMessage(),
                                "tool_name", "razorpay_payment_link"
                        ));
                        throw linkEx; // Re-throw — outer catch will schedule retry in 15min
                    }
                    if (url == null || url.isBlank()) {
                        // Razorpay returned 200 but no short_url — treat as failure
                        String msg = "Razorpay returned no short_url in response — possible auth or field validation error";
                        log.error("[TOOL-EXEC] PAYMENT LINK NULL URL for case={} action={} — {}", rc.getId(), action, msg);
                        auditService.logEvent(rc.getId(), "TOOL_EXECUTION", Map.of(
                                "action", action,
                                "idempotency_key", idempotencyKey,
                                "result", "FAILED: " + msg,
                                "tool_name", "razorpay_payment_link"
                        ));
                        throw new RuntimeException(msg);
                    }
                    rc.setPaymentLinkUrl(url);
                    rc.setStatus(CaseState.WAITING);
                    rc.setUpdatedAt(OffsetDateTime.now());
                    // === GREPPABLE PAYMENT LINK LOG — must be its own distinct line ===
                    log.info("[TOOL_EXECUTION] Payment link created for case={} : {}", rc.getId(), url);
                    auditService.logEvent(rc.getId(), "TOOL_EXECUTION", Map.of(
                            "action", action,
                            "idempotency_key", idempotencyKey,
                            "result", "SUCCESS — payment link created",
                            "short_url", url,
                            "tool_name", "razorpay_payment_link"
                    ));
                    auditService.logEvent(rc.getId(), "OUTCOME", Map.of(
                            "status", "WAITING",
                            "reason", String.format(
                                "Payment link generated and sent to customer. Customer must complete payment at %s. " +
                                "Case will be marked RECOVERED automatically when payment_link.paid webhook is received.",
                                url),
                            "action_taken", action,
                            "short_url", url
                    ));
                    log.info("[TOOL-EXEC] Case id={} -> WAITING via {} url={}", rc.getId(), action, url);
                    break;

                case "SEND_REMINDER":
                case "SEND_SMS":
                case "SEND_WHATSAPP":
                case "SEND_EMAIL":
                    notificationSender.send(action, payment.getCustomerId(), idempotencyKey,
                            payment.getContact(), payment.getEmail());
                    rc.setStatus(CaseState.WAITING);
                    // Schedule next evaluation in 24h (within the 72h window)
                    rc.setNextRunAt(OffsetDateTime.now().plusHours(24));
                    rc.setUpdatedAt(OffsetDateTime.now());
                    auditService.logEvent(rc.getId(), "TOOL_EXECUTION", Map.of(
                            "action", action,
                            "idempotency_key", idempotencyKey,
                            "result", "SUCCESS — notification sent",
                            "tool_name", "notification_sender"
                    ));
                    // Outcome text is per action type — never claim recovery until confirmed by webhook
                    String notifOutcomeReason = switch (action) {
                        case "SEND_EMAIL" -> String.format(
                                "Reminder sent to customer via email. Awaiting customer retry attempt. " +
                                "Case will re-evaluate at %s or stop if the 72h recovery window closes first.",
                                rc.getNextRunAt());
                        case "SEND_SMS" -> String.format(
                                "Reminder sent to customer via SMS. Awaiting customer retry attempt. " +
                                "Case will re-evaluate at %s or stop if the 72h recovery window closes first.",
                                rc.getNextRunAt());
                        case "SEND_WHATSAPP" -> String.format(
                                "Reminder sent to customer via WhatsApp. Awaiting customer retry attempt. " +
                                "Case will re-evaluate at %s or stop if the 72h recovery window closes first.",
                                rc.getNextRunAt());
                        default -> String.format(
                                "Reminder dispatched to customer. Awaiting customer retry attempt. " +
                                "Case will re-evaluate at %s or stop if the 72h recovery window closes first.",
                                rc.getNextRunAt());
                    };
                    auditService.logEvent(rc.getId(), "OUTCOME", Map.of(
                            "status", "WAITING",
                            "reason", notifOutcomeReason,
                            "action_taken", action
                    ));
                    log.info("[TOOL-EXEC] Case id={} -> WAITING via notification {} nextRunAt={}", rc.getId(), action, rc.getNextRunAt());
                    break;

                case "ESCALATE":
                    escalationService.escalate(rc.getId().toString(), idempotencyKey);
                    rc.setStatus(CaseState.ESCALATED);
                    rc.setUpdatedAt(OffsetDateTime.now());
                    auditService.logEvent(rc.getId(), "TOOL_EXECUTION", Map.of(
                            "action", action,
                            "idempotency_key", idempotencyKey,
                            "result", "SUCCESS — escalated",
                            "tool_name", "escalation_service"
                    ));
                    auditService.logEvent(rc.getId(), "OUTCOME", Map.of(
                            "status", "ESCALATED",
                            "reason", "Case escalated for human review",
                            "action_taken", action
                    ));
                    log.info("[TOOL-EXEC] Case id={} -> ESCALATED", rc.getId());
                    break;

                case "STOP":
                    stopRecoveryHandler.stop(rc.getId().toString(), idempotencyKey);
                    rc.setStatus(CaseState.STOPPED);
                    rc.setUpdatedAt(OffsetDateTime.now());
                    auditService.logEvent(rc.getId(), "TOOL_EXECUTION", Map.of(
                            "action", action,
                            "idempotency_key", idempotencyKey,
                            "result", "SUCCESS — stopped",
                            "tool_name", "stop_recovery_handler"
                    ));
                    auditService.logEvent(rc.getId(), "OUTCOME", Map.of(
                            "status", "STOPPED",
                            "reason", "Recovery explicitly stopped",
                            "action_taken", action
                    ));
                    log.info("[TOOL-EXEC] Case id={} -> STOPPED", rc.getId());
                    break;

                default:
                    log.warn("[TOOL-EXEC] Unknown action '{}' for case id={}, stopping recovery.", action, rc.getId());
                    rc.setStatus(CaseState.STOPPED);
                    rc.setUpdatedAt(OffsetDateTime.now());
                    auditService.logEvent(rc.getId(), "OUTCOME", Map.of(
                            "status", "STOPPED",
                            "reason", "Unknown action: " + action
                    ));
                    break;
            }

        } catch (Exception e) {
            log.error("[TOOL-EXEC] Case id={} action={} FAILED — scheduling retry in 15min", rc.getId(), action, e);

            auditService.logEvent(rc.getId(), "TOOL_EXECUTION", Map.of(
                    "action", action,
                    "idempotency_key", idempotencyKey,
                    "result", "FAILED: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())
            ));

            // Back-off: return to ELIGIBLE so orchestrator retries after delay
            rc.setStatus(CaseState.ELIGIBLE);
            rc.setNextRunAt(OffsetDateTime.now().plusMinutes(15));
            rc.setUpdatedAt(OffsetDateTime.now());
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
