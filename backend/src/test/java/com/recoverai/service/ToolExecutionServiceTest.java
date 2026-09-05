package com.recoverai.service;

import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.CaseState;
import com.recoverai.domain.recovery.RecoveryCase;
import com.recoverai.integration.EscalationService;
import com.recoverai.integration.StopRecoveryHandler;
import com.recoverai.integration.notification.NotificationSender;
import com.recoverai.integration.razorpay.PaymentLinkService;
import com.recoverai.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToolExecutionServiceTest {

    @Mock
    private PaymentLinkService paymentLinkService;
    @Mock
    private NotificationSender notificationSender;
    @Mock
    private EscalationService escalationService;
    @Mock
    private StopRecoveryHandler stopRecoveryHandler;
    @Mock
    private AuditService auditService;
    @Mock
    private AuditEventRepository auditEventRepository;

    @InjectMocks
    private ToolExecutionService toolExecutionService;

    @Test
    void execute_ShouldExecuteRetryAndSetWaiting() {
        UUID caseId = UUID.randomUUID();
        RecoveryCase rc = new RecoveryCase();
        rc.setId(caseId);
        rc.setChosenAction("RETRY");
        rc.setStatus(CaseState.ACTION_EXECUTING);

        Payment payment = new Payment();
        payment.setId("pay_123");

        // No past actions -> attempt number = 1
        when(auditEventRepository.findByCaseIdOrderBySequenceNoAsc(caseId)).thenReturn(List.of());

        toolExecutionService.execute(rc, payment);

        verify(paymentLinkService).createPaymentLink(eq(rc), eq(payment), eq(caseId + "-RETRY-1"));
        verify(auditService).logEvent(eq(caseId), eq("TOOL_EXECUTION"), anyMap());
        verify(auditService).logEvent(eq(caseId), eq("OUTCOME"), anyMap());

        assertThat(rc.getStatus()).isEqualTo(CaseState.WAITING);
    }

    @Test
    void execute_ShouldExecuteSendSmsAndSetWaiting() {
        UUID caseId = UUID.randomUUID();
        RecoveryCase rc = new RecoveryCase();
        rc.setId(caseId);
        rc.setChosenAction("SEND_SMS");
        rc.setStatus(CaseState.ACTION_EXECUTING);

        Payment payment = new Payment();
        payment.setId("pay_123");
        payment.setCustomerId("cust_123");

        when(auditEventRepository.findByCaseIdOrderBySequenceNoAsc(caseId)).thenReturn(List.of());

        toolExecutionService.execute(rc, payment);

        verify(notificationSender).send(eq("SEND_SMS"), eq("cust_123"), eq(caseId + "-SEND_SMS-1"), any(), any());
        assertThat(rc.getStatus()).isEqualTo(CaseState.WAITING);
    }

    @Test
    void execute_ShouldHandleToolFailureAndSetDiagnosing() {
        UUID caseId = UUID.randomUUID();
        RecoveryCase rc = new RecoveryCase();
        rc.setId(caseId);
        rc.setChosenAction("RETRY");
        rc.setStatus(CaseState.ACTION_EXECUTING);

        Payment payment = new Payment();
        payment.setId("pay_123");

        when(auditEventRepository.findByCaseIdOrderBySequenceNoAsc(caseId)).thenReturn(List.of());

        doThrow(new RuntimeException("Razorpay API Error: 500")).when(paymentLinkService).createPaymentLink(any(), any(), any());

        toolExecutionService.execute(rc, payment);

        verify(auditService).logEvent(eq(caseId), eq("TOOL_EXECUTION"), argThat(obj -> obj instanceof java.util.Map && ((java.util.Map<?,?>) obj).get("result").equals("FAILED — Razorpay API Error: 500")));
        
        assertThat(rc.getStatus()).isEqualTo(CaseState.DIAGNOSING);
        assertThat(rc.getNextRunAt()).isNotNull();
    }
}
