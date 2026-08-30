package com.recoverai.policy;

import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompliancePolicyTest {

    private final CompliancePolicy policy = new CompliancePolicy();

    @Test
    void shouldBlockSmsForDndCustomer() {
        Payment p = new Payment();
        p.setCustomerId("user123_dnd");

        RecoveryCase rc = new RecoveryCase();
        
        boolean permitted = policy.isPermitted(rc, p, "SEND_SMS", List.of());
        assertThat(permitted).isFalse();
        assertThat(policy.getReason()).contains("DND");
    }

    @Test
    void shouldPermitSmsForNormalCustomer() {
        Payment p = new Payment();
        p.setCustomerId("user123");

        RecoveryCase rc = new RecoveryCase();
        
        boolean permitted = policy.isPermitted(rc, p, "SEND_SMS", List.of());
        assertThat(permitted).isTrue();
    }

    @Test
    void shouldBlockMandateRetryWithoutLeadTime() {
        Payment p = new Payment();
        p.setMethod("MANDATE");

        RecoveryCase rc = new RecoveryCase();
        rc.setCreatedAt(OffsetDateTime.now().minusHours(10)); // Less than 24h
        
        boolean permitted = policy.isPermitted(rc, p, "RETRY", List.of());
        assertThat(permitted).isFalse();
        assertThat(policy.getReason()).contains("24h lead time");
    }

    @Test
    void shouldPermitMandateRetryWithLeadTime() {
        Payment p = new Payment();
        p.setMethod("MANDATE");

        RecoveryCase rc = new RecoveryCase();
        rc.setCreatedAt(OffsetDateTime.now().minusHours(25)); // More than 24h
        
        boolean permitted = policy.isPermitted(rc, p, "RETRY", List.of());
        assertThat(permitted).isTrue();
    }
}
