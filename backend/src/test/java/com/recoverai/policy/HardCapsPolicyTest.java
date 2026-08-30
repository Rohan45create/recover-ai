package com.recoverai.policy;

import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HardCapsPolicyTest {

    private final HardCapsPolicy policy = new HardCapsPolicy();

    @Test
    void shouldPermitValidAction() {
        Payment p = new Payment();
        p.setAmount(new BigDecimal("5000"));
        
        RecoveryCase rc = new RecoveryCase();
        rc.setCreatedAt(OffsetDateTime.now().minusHours(10));
        
        boolean permitted = policy.isPermitted(rc, p, "RETRY", List.of());
        assertThat(permitted).isTrue();
    }

    @Test
    void shouldBlockWhenAmountExceeds25K() {
        Payment p = new Payment();
        p.setAmount(new BigDecimal("25001"));
        
        RecoveryCase rc = new RecoveryCase();
        rc.setCreatedAt(OffsetDateTime.now().minusHours(10));
        
        boolean permitted = policy.isPermitted(rc, p, "RETRY", List.of());
        assertThat(permitted).isFalse();
        assertThat(policy.getReason()).contains("₹25,000");
    }

    @Test
    void shouldBlockWhenWindowExceeds72h() {
        Payment p = new Payment();
        p.setAmount(new BigDecimal("5000"));
        
        RecoveryCase rc = new RecoveryCase();
        rc.setCreatedAt(OffsetDateTime.now().minusHours(73));
        
        boolean permitted = policy.isPermitted(rc, p, "RETRY", List.of());
        assertThat(permitted).isFalse();
        assertThat(policy.getReason()).contains("72h window");
    }

    @Test
    void shouldBlockWhenMaxRetriesReached() {
        Payment p = new Payment();
        p.setAmount(new BigDecimal("5000"));
        
        RecoveryCase rc = new RecoveryCase();
        rc.setCreatedAt(OffsetDateTime.now().minusHours(10));
        
        AuditEvent e1 = new AuditEvent();
        e1.setEventType("STATE_CHANGED");
        e1.setPayload("{\"chosen_action\":\"RETRY\"}");

        AuditEvent e2 = new AuditEvent();
        e2.setEventType("STATE_CHANGED");
        e2.setPayload("{\"chosen_action\":\"RETRY\"}");

        boolean permitted = policy.isPermitted(rc, p, "RETRY", List.of(e1, e2));
        assertThat(permitted).isFalse();
        assertThat(policy.getReason()).contains("retry attempts");
    }

    @Test
    void shouldBlockWhenMaxMessagesReached() {
        Payment p = new Payment();
        p.setAmount(new BigDecimal("5000"));
        
        RecoveryCase rc = new RecoveryCase();
        rc.setCreatedAt(OffsetDateTime.now().minusHours(10));
        
        AuditEvent e1 = new AuditEvent();
        e1.setEventType("STATE_CHANGED");
        e1.setPayload("{\"chosen_action\":\"SEND_SMS\"}");

        AuditEvent e2 = new AuditEvent();
        e2.setEventType("STATE_CHANGED");
        e2.setPayload("{\"chosen_action\":\"SEND_WHATSAPP\"}");

        AuditEvent e3 = new AuditEvent();
        e3.setEventType("STATE_CHANGED");
        e3.setPayload("{\"chosen_action\":\"SEND_EMAIL\"}");

        boolean permitted = policy.isPermitted(rc, p, "SEND_SMS", List.of(e1, e2, e3));
        assertThat(permitted).isFalse();
        assertThat(policy.getReason()).contains("message attempts");
    }
}
