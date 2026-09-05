package com.recoverai.service;

import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionServiceTest {

    private final DecisionService decisionService = new DecisionService();

    @Test
    void shouldSelectHighestExpectedValueAction() {
        Payment p = new Payment();
        p.setAmount(new BigDecimal("1000"));

        RecoveryCase rc = new RecoveryCase();
        rc.setDiagnosis("INSUFFICIENT_FUNDS");

        // RETRY EV: 1000 * 0.40 - 0 = 400
        // SEND_SMS EV: 1000 * 0.15 - 1.00 = 149
        // SEND_WHATSAPP EV: 1000 * 0.20 - 2.50 = 197.50

        DecisionResult result = decisionService.selectBestAction(rc, p, List.of("RETRY", "SEND_SMS", "SEND_WHATSAPP"));

        assertThat(result.getChosenAction()).isEqualTo("RETRY");
        assertThat(result.getExpectedRecoveryValue()).isEqualByComparingTo(new BigDecimal("400.00"));
    }

    @Test
    void shouldReturnNoneIfNoProfitableAction() {
        Payment p = new Payment();
        p.setAmount(new BigDecimal("1")); // 1 rupee

        RecoveryCase rc = new RecoveryCase();
        rc.setDiagnosis("INSUFFICIENT_FUNDS");

        // RETRY EV: 1 * 0.40 = 0.40 (this is > 0, so it would be chosen)
        // Wait, let's make it FRAUD_SUSPECTED
        rc.setDiagnosis("FRAUD_SUSPECTED");

        // FRAUD EV for anything is 0. With cost it is negative.
        DecisionResult result = decisionService.selectBestAction(rc, p, List.of("RETRY", "SEND_SMS"));

        assertThat(result.getChosenAction()).isEqualTo("NONE");
        assertThat(result.getExpectedRecoveryValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
