package com.recoverai.policy;

import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class HardCapsPolicy implements PolicyRule {

    private static final BigDecimal MAX_AUTOMATED_RECOVERY = new BigDecimal("25000");
    private static final int MAX_RETRIES = 2;
    private static final int MAX_MESSAGES = 3;
    private static final int MAX_WINDOW_HOURS = 72;
    
    private String reason = "";

    @Override
    public boolean isPermitted(RecoveryCase rc, Payment payment, String candidateAction, List<AuditEvent> history) {
        
        // 1. Max Automated Recovery
        if (payment.getAmount() != null && payment.getAmount().compareTo(MAX_AUTOMATED_RECOVERY) > 0) {
            reason = "Payment amount exceeds maximum automated recovery limit of ₹25,000";
            return false;
        }
        
        // 2. 72h Window
        if (rc.getCreatedAt().plusHours(MAX_WINDOW_HOURS).isBefore(OffsetDateTime.now())) {
            reason = "Recovery case has exceeded the 72h window";
            return false;
        }

        // 3. Max Retries & Messages
        long retryCount = countActionsInHistory(history, "RETRY");
        long messageCount = countActionsInHistory(history, "SEND_SMS") + 
                            countActionsInHistory(history, "SEND_EMAIL") + 
                            countActionsInHistory(history, "SEND_WHATSAPP");

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

    private long countActionsInHistory(List<AuditEvent> history, String actionType) {
        // We look for STATE_CHANGED events where chosen_action equals the target action
        // Or ACTION_EXECUTED, but phase 2 just logged STATE_CHANGED with chosen_action
        return history.stream()
            .filter(e -> "STATE_CHANGED".equals(e.getEventType()))
            .filter(e -> {
                String payload = e.getPayload();
                return payload != null && payload.contains("\"chosen_action\":\"" + actionType + "\"");
            })
            .count();
    }

    @Override
    public String getReason() {
        return reason;
    }
}
