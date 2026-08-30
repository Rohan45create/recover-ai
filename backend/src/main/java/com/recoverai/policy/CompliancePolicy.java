package com.recoverai.policy;

import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CompliancePolicy implements PolicyRule {

    private String reason = "";

    @Override
    public boolean isPermitted(RecoveryCase rc, Payment payment, String candidateAction, List<AuditEvent> history) {
        
        // 1. CONSENT_REQUIRED_FOR_SMS_WHATSAPP
        if ("SEND_SMS".equals(candidateAction) || "SEND_WHATSAPP".equals(candidateAction)) {
            // Stub: if customerId ends with _dnd, they opted out
            if (payment.getCustomerId() != null && payment.getCustomerId().endsWith("_dnd")) {
                reason = "Customer has opted out of messaging (DND)";
                return false;
            }
        }
        
        // 2. RECURRING_MANDATE_RETRY_WINDOW
        if ("MANDATE".equalsIgnoreCase(payment.getMethod()) && "RETRY".equals(candidateAction)) {
            // Stub: enforce a 24h lead time for mandate retries based on case creation
            // (In a real system, you'd check last debit attempt)
            if (rc.getCreatedAt().plusHours(24).isAfter(java.time.OffsetDateTime.now())) {
                reason = "Mandate retry requires a 24h lead time notice";
                return false;
            }
        }

        return true;
    }

    @Override
    public String getReason() {
        return reason;
    }
}
