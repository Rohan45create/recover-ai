package com.recoverai.controller;

import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.CaseState;
import com.recoverai.domain.recovery.RecoveryCase;
import com.recoverai.dto.DashboardOverviewResponse;
import com.recoverai.repository.PaymentRepository;
import com.recoverai.repository.RecoveryCaseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.recoverai.TestcontainersConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
public class DashboardControllerTest {

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private RecoveryCaseRepository recoveryCaseRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    public void testOverviewTrajectoryIncludesToday() {
        // Given a payment and a recovery case created right now
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setAmount(new BigDecimal("1234.50"));
        payment.setCurrency("INR");
        payment.setCreatedAt(OffsetDateTime.now());
        paymentRepository.save(payment);

        RecoveryCase rc = new RecoveryCase();
        rc.setId(UUID.randomUUID());
        rc.setPaymentId(payment.getId());
        rc.setStatus(CaseState.RECOVERED);
        rc.setCreatedAt(OffsetDateTime.now());
        rc.setUpdatedAt(OffsetDateTime.now());
        recoveryCaseRepository.save(rc);

        String todayFormatted = OffsetDateTime.now().toLocalDate().toString(); // YYYY-MM-DD

        // When
        DashboardOverviewResponse response = dashboardController.getOverview("7d");

        // Then
        assertNotNull(response.getTrajectory());
        boolean foundToday = response.getTrajectory().stream()
                .anyMatch(point -> point.getDay().equals(todayFormatted) && point.getAmount().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(foundToday, "Trajectory should contain today's date with a positive amount");
    }
}
