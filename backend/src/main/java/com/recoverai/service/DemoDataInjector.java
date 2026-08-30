package com.recoverai.service;

import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.CaseState;
import com.recoverai.domain.recovery.RecoveryCase;
import com.recoverai.evaluation.DatasetGenerator;
import com.recoverai.repository.AuditEventRepository;
import com.recoverai.repository.PaymentRepository;
import com.recoverai.repository.RecoveryCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test") // Don't run this during tests to avoid interfering with test data
public class DemoDataInjector implements CommandLineRunner {

    private final RecoveryCaseRepository recoveryCaseRepository;
    private final PaymentRepository paymentRepository;
    private final AuditEventRepository auditEventRepository;
    private final DatasetGenerator datasetGenerator;
    private final AuditService auditService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (recoveryCaseRepository.count() > 0) {
            log.info("Database already contains recovery cases. Skipping demo data injection.");
            return;
        }

        log.info("Database is empty. Injecting 200 synthetic cases for demo dashboard...");
        
        List<DatasetGenerator.SyntheticCase> syntheticCases = datasetGenerator.generate(200, 1337L);
        
        for (DatasetGenerator.SyntheticCase sc : syntheticCases) {
            Payment payment = sc.getPayment();
            payment.setCurrency("INR");
            
            // Spread cases backwards over the last 30 days randomly
            int daysAgo = (int) (Math.random() * 30);
            java.time.OffsetDateTime timestamp = java.time.OffsetDateTime.now().minusDays(daysAgo);
            
            payment.setCreatedAt(timestamp);
            RecoveryCase rc = sc.getRecoveryCase();
            rc.setCreatedAt(timestamp); // Assuming there's a setCreatedAt for RecoveryCase
            rc.setUpdatedAt(timestamp);
            
            // Randomize the status for the dashboard
            double roll = Math.random();
            if (roll < 0.2) {
                rc.setStatus(CaseState.RECOVERED);
            } else if (roll < 0.3) {
                rc.setStatus(CaseState.STOPPED);
            } else if (roll < 0.5) {
                rc.setStatus(CaseState.DIAGNOSING);
            } else if (roll < 0.7) {
                rc.setStatus(CaseState.ACTION_EXECUTING);
                rc.setChosenAction("SEND_SMS");
            } else {
                rc.setStatus(CaseState.WAITING);
                rc.setChosenAction("RETRY");
            }
            
            paymentRepository.save(payment);
            recoveryCaseRepository.save(rc);
            
            auditService.logEvent(rc.getId(), "CASE_CREATED", 
                java.util.Map.of(
                    "payment_id", payment.getId(),
                    "diagnosis", rc.getDiagnosis(),
                    "amount", payment.getAmount()
                ));
        }
        
        log.info("Demo data injection complete.");
    }
}
