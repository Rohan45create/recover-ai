package com.recoverai.policy;

import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Blocks any action that requires reaching the customer when no contact information
 * has been captured for the payment. This is a hard policy block, not a silent no-op.
 *
 * <p>Block reason logged to audit_events: "NO_CONTACT_INFO"</p>
 *
 * <p>Actions that require contact info:
 * SEND_REMINDER, SEND_SMS, SEND_WHATSAPP, SEND_EMAIL, CREATE_PAYMENT_LINK</p>
 */
@Component
public class ContactInfoPolicy implements PolicyRule {

    private static final Set<String> CONTACT_REQUIRED_ACTIONS = Set.of(
            "SEND_REMINDER",
            "SEND_SMS",
            "SEND_WHATSAPP",
            "SEND_EMAIL",
            "CREATE_PAYMENT_LINK"
    );

    private String reason = "";

    @Override
    public boolean isPermitted(RecoveryCase rc, Payment payment, String candidateAction, List<AuditEvent> history) {
        if (!CONTACT_REQUIRED_ACTIONS.contains(candidateAction)) {
            return true; // Action doesn't need contact info — pass through
        }

        boolean hasContact = payment.getContact() != null && !payment.getContact().isBlank();
        boolean hasEmail   = payment.getEmail()   != null && !payment.getEmail().isBlank();

        if (!hasContact && !hasEmail) {
            reason = "NO_CONTACT_INFO";
            return false;
        }

        return true;
    }

    @Override
    public String getReason() {
        return reason;
    }
}
