package com.recoverai.service;

import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.CaseState;
import com.recoverai.domain.recovery.RecoveryCase;
import com.recoverai.dto.RazorpayWebhookPayload;
import com.recoverai.repository.PaymentRepository;
import com.recoverai.repository.RecoveryCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RecoveryCaseRepository recoveryCaseRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private IngestionService ingestionService;

    @Test
    void processWebhook_ShouldCreatePaymentAndRecoveryCase_WhenEventIsPaymentFailed() {
        // Arrange
        RazorpayWebhookPayload payload = new RazorpayWebhookPayload();
        payload.setEvent("payment.failed");

        RazorpayWebhookPayload.Entity entity = new RazorpayWebhookPayload.Entity();
        entity.setId("pay_123");
        entity.setAmount(BigDecimal.valueOf(1000));
        entity.setCurrency("INR");
        entity.setStatus("failed");
        entity.setErrorCode("BAD_REQUEST_ERROR");
        
        RazorpayWebhookPayload.PaymentEntity paymentEntity = new RazorpayWebhookPayload.PaymentEntity();
        paymentEntity.setEntity(entity);
        
        RazorpayWebhookPayload.Payload payloadWrapper = new RazorpayWebhookPayload.Payload();
        payloadWrapper.setPayment(paymentEntity);
        
        payload.setPayload(payloadWrapper);

        // Act
        ingestionService.processWebhook(payload);

        // Assert
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getId()).isEqualTo("pay_123");
        assertThat(savedPayment.getAmount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(savedPayment.getEmail()).isNull(); // entity had no email set

        ArgumentCaptor<RecoveryCase> caseCaptor = ArgumentCaptor.forClass(RecoveryCase.class);
        verify(recoveryCaseRepository).save(caseCaptor.capture());
        RecoveryCase savedCase = caseCaptor.getValue();
        assertThat(savedCase.getPaymentId()).isEqualTo("pay_123");
        assertThat(savedCase.getStatus()).isEqualTo(CaseState.ELIGIBLE);

        verify(auditService).logEvent(eq(savedCase.getId()), eq("WEBHOOK_VERIFIED"), any(Map.class));
    }

    @Test
    void processWebhook_ShouldIgnore_WhenEventIsNotPaymentFailed() {
        // Arrange
        RazorpayWebhookPayload payload = new RazorpayWebhookPayload();
        payload.setEvent("payment.captured");

        // Act
        ingestionService.processWebhook(payload);

        // Assert
        verifyNoInteractions(paymentRepository, recoveryCaseRepository, auditService);
    }
}
