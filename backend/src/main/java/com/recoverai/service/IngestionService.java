package com.recoverai.service;

import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.CaseState;
import com.recoverai.domain.recovery.RecoveryCase;
import com.recoverai.dto.RazorpayWebhookPayload;
import com.recoverai.repository.PaymentRepository;
import com.recoverai.repository.RecoveryCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final PaymentRepository paymentRepository;
    private final RecoveryCaseRepository recoveryCaseRepository;
    private final AuditService auditService;

    @Transactional
    public void processWebhook(RazorpayWebhookPayload payload) {
        if (!"payment.failed".equals(payload.getEvent())) {
            return;
        }

        RazorpayWebhookPayload.Entity entity = payload.getPayload().getPayment().getEntity();

        // 1. Persist the Payment
        Payment payment = Payment.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .method(entity.getMethod())
                .customerId(entity.getCustomerId())
                .contact(entity.getContact())
                .errorCode(entity.getErrorCode())
                .errorDescription(entity.getErrorDescription())
                .createdAt(OffsetDateTime.now())
                .build();
        paymentRepository.save(payment);

        // 2. Create RecoveryCase (RECEIVED -> ELIGIBLE)
        UUID caseId = UUID.randomUUID();
        RecoveryCase recoveryCase = RecoveryCase.builder()
                .id(caseId)
                .paymentId(payment.getId())
                .status(CaseState.ELIGIBLE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        recoveryCaseRepository.save(recoveryCase);

        // 3. Write Audit Events
        auditService.logEvent(caseId, "WEBHOOK_VERIFIED", Map.of(
            "source", "razorpay",
            "signature_verified", true,
            "event", payload.getEvent() != null ? payload.getEvent() : "payment.failed",
            "payment_id", payment.getId() != null ? payment.getId() : "",
            "amount", payment.getAmount() != null ? payment.getAmount() : 0,
            "error_code", payment.getErrorCode() != null ? payment.getErrorCode() : "UNKNOWN"
        ));
        
    }
}
