package com.recoverai.policy;

import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;

import java.util.List;

public interface PolicyRule {
    boolean isPermitted(RecoveryCase rc, Payment payment, String candidateAction, List<AuditEvent> history);
    String getReason();

    /** True when the payment should be routed to ESCALATED status rather than STOPPED. */
    default boolean isEscalationRequired() {
        return false;
    }
}
