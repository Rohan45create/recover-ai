package com.recoverai.policy;

import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.policy.Policy;
import com.recoverai.domain.recovery.RecoveryCase;
import com.recoverai.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HardCapsPolicy implements PolicyRule {

    // Fallback constants used only if DB rows are missing (should not happen after V4 migration)
    private static final BigDecimal DEFAULT_MAX_AUTOMATED_RECOVERY = new BigDecimal("25000");
    private static final BigDecimal DEFAULT_ESCALATION_THRESHOLD   = new BigDecimal("30000");
    private static final int MAX_RETRIES      = 2;
    private static final int MAX_MESSAGES     = 3;
    private static final int MAX_WINDOW_HOURS = 72;

    private final PolicyRepository policyRepository;

    // Per-evaluation state — reset at the start of each isPermitted() call
    private String  reason             = "";
    private boolean escalationRequired = false;

    @Override
    public boolean isPermitted(RecoveryCase rc, Payment payment, String candidateAction, List<AuditEvent> history) {
        // Reset per-call state
        reason             = "";
        escalationRequired = false;

        // Read limits live from DB (no caching — edits on the Policies page take effect immediately)
        BigDecimal maxRecoveryCost = readDecimalPolicy("POL-01", DEFAULT_MAX_AUTOMATED_RECOVERY);

        Policy pol04        = policyRepository.findById("POL-04").orElse(null);
        boolean pol04On     = pol04 != null && Boolean.TRUE.equals(pol04.getEnabled());
        BigDecimal escThreshold = pol04 != null
                ? parseDecimalSafe(pol04.getValue(), DEFAULT_ESCALATION_THRESHOLD)
                : DEFAULT_ESCALATION_THRESHOLD;

        BigDecimal amount = payment.getAmount();

        // 1. Escalation Threshold (POL-04) — checked first so ESCALATED takes precedence over STOPPED
        if (amount != null && pol04On && amount.compareTo(escThreshold) > 0) {
            reason = String.format(
                    "Payment amount Rs.%s exceeds escalation threshold of Rs.%s — routed to human queue",
                    amount.toPlainString(), escThreshold.toPlainString());
            escalationRequired = true;
            return false;
        }

        // 2. Max Automated Recovery (POL-01)
        if (amount != null && amount.compareTo(maxRecoveryCost) > 0) {
            reason = String.format(
                    "Payment amount Rs.%s exceeds maximum automated recovery limit of Rs.%s",
                    amount.toPlainString(), maxRecoveryCost.toPlainString());
            return false;
        }

        // 3. 72-hour Window
        if (rc.getCreatedAt().plusHours(MAX_WINDOW_HOURS).isBefore(OffsetDateTime.now())) {
            reason = "Recovery case has exceeded the 72h window";
            return false;
        }

        // 4. Max Retries & Messages
        long retryCount   = countActionsInHistory(history, "RETRY");
        long messageCount = countActionsInHistory(history, "SEND_SMS")
                          + countActionsInHistory(history, "SEND_EMAIL")
                          + countActionsInHistory(history, "SEND_WHATSAPP");

        if ("RETRY".equals(candidateAction)) {
            if (retryCount >= MAX_RETRIES) {
                reason = "Maximum retry attempts (2) reached";
                return false;
            }
        } else if (candidateAction.startsWith("SEND_")) {
            if (messageCount >= MAX_MESSAGES) {
                reason = "Maximum message attempts (3) reached";
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEscalationRequired() {
        return escalationRequired;
    }

    @Override
    public String getReason() {
        return reason;
    }

    private BigDecimal readDecimalPolicy(String policyId, BigDecimal fallback) {
        return policyRepository.findById(policyId)
                .map(p -> parseDecimalSafe(p.getValue(), fallback))
                .orElse(fallback);
    }

    private BigDecimal parseDecimalSafe(String value, BigDecimal fallback) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private long countActionsInHistory(List<AuditEvent> history, String actionType) {
        return history.stream()
                .filter(e -> "STATE_CHANGED".equals(e.getEventType()))
                .filter(e -> {
                    String payload = e.getPayload();
                    return payload != null && payload.contains("\"chosen_action\":\"" + actionType + "\"");
                })
                .count();
    }
}
