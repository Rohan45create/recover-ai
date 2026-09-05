package com.recoverai.policy;

import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.policy.Policy;
import com.recoverai.domain.recovery.CaseState;
import com.recoverai.domain.recovery.RecoveryCase;
import com.recoverai.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HardCapsPolicyTest {

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private HardCapsPolicy hardCapsPolicy;

    private RecoveryCase rc;
    private List<AuditEvent> emptyHistory;

    @BeforeEach
    void setUp() {
        rc = RecoveryCase.builder()
                .id(UUID.randomUUID())
                .paymentId("pay_test")
                .status(CaseState.ELIGIBLE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        emptyHistory = List.of();

        // Default: POL-01 = 25000, POL-04 = 30000 enabled
        stubPol01("25000");
        stubPol04("30000", true);
    }

    // -- Bug #1 regression tests ----------------------------------------------

    @Test
    void pol01_allows_payment_below_max_recovery_cost() {
        Payment payment = paymentOf("20000");

        boolean permitted = hardCapsPolicy.isPermitted(rc, payment, "SEND_EMAIL", emptyHistory);

        assertThat(permitted).isTrue();
        assertThat(hardCapsPolicy.isEscalationRequired()).isFalse();
    }

    @Test
    void pol01_blocks_payment_above_max_recovery_cost() {
        // 30000 > POL-01 limit of 25000 AND exceeds POL-04 threshold -> escalation, not mere block
        // Use 26000 to test the pure POL-01 block path (between 25000 and 30000 escalation threshold)
        // POL-04 must be disabled for this test
        stubPol04("30000", false);
        Payment payment = paymentOf("26000");

        boolean permitted = hardCapsPolicy.isPermitted(rc, payment, "SEND_EMAIL", emptyHistory);

        assertThat(permitted).isFalse();
        assertThat(hardCapsPolicy.isEscalationRequired()).isFalse();
        assertThat(hardCapsPolicy.getReason()).contains("25000");
    }

    // -- POL-04 escalation tests ----------------------------------------------

    @Test
    void pol04_does_not_escalate_payment_below_threshold() {
        Payment payment = paymentOf("20000"); // below both POL-01 (25000) and POL-04 (30000)

        boolean permitted = hardCapsPolicy.isPermitted(rc, payment, "SEND_EMAIL", emptyHistory);

        assertThat(permitted).isTrue();
        assertThat(hardCapsPolicy.isEscalationRequired()).isFalse();
    }

    @Test
    void pol04_escalates_payment_above_threshold_when_enabled() {
        Payment payment = paymentOf("35000"); // above POL-04 threshold of 30000

        boolean permitted = hardCapsPolicy.isPermitted(rc, payment, "SEND_EMAIL", emptyHistory);

        assertThat(permitted).isFalse();
        assertThat(hardCapsPolicy.isEscalationRequired()).isTrue();
        assertThat(hardCapsPolicy.getReason()).contains("30000");
        assertThat(hardCapsPolicy.getReason()).containsIgnoringCase("escalat");
    }

    @Test
    void pol04_disabled_does_not_escalate() {
        stubPol04("30000", false); // POL-04 off
        Payment payment = paymentOf("35000"); // would exceed threshold but POL-04 is off

        // Amount 35000 > POL-01 limit 25000 -> blocked but NOT escalated
        boolean permitted = hardCapsPolicy.isPermitted(rc, payment, "SEND_EMAIL", emptyHistory);

        assertThat(permitted).isFalse();
        assertThat(hardCapsPolicy.isEscalationRequired()).isFalse();
        assertThat(hardCapsPolicy.getReason()).contains("25000");
    }

    // -- Helpers --------------------------------------------------------------

    private Payment paymentOf(String amount) {
        return Payment.builder()
                .id("pay_test_" + amount)
                .amount(new BigDecimal(amount))
                .currency("INR")
                .status("failed")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    private void stubPol01(String value) {
        when(policyRepository.findById("POL-01"))
                .thenReturn(Optional.of(Policy.builder()
                        .id("POL-01").name("Max Recovery Cost")
                        .value(value).unit("INR").enabled(true)
                        .updatedAt(OffsetDateTime.now()).updatedBy("system")
                        .build()));
    }

    private void stubPol04(String value, boolean enabled) {
        when(policyRepository.findById("POL-04"))
                .thenReturn(Optional.of(Policy.builder()
                        .id("POL-04").name("Escalation Threshold")
                        .value(value).unit("INR").enabled(enabled)
                        .updatedAt(OffsetDateTime.now()).updatedBy("system")
                        .build()));
    }
}
