package com.recoverai.service;

import com.recoverai.ai.AiDiagnosisResponse;
import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.service.DecisionResult;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.CaseState;
import com.recoverai.domain.recovery.RecoveryCase;
import com.recoverai.policy.PolicyEngine;
import com.recoverai.policy.PolicyEvaluationResult;
import com.recoverai.repository.AuditEventRepository;
import com.recoverai.repository.PaymentRepository;
import com.recoverai.repository.RecoveryCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecoveryCaseProcessorTest {

    @Mock
    private RecoveryCaseRepository recoveryCaseRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private DiagnosisService diagnosisService;

    @Mock
    private PolicyEngine policyEngine;

    @Mock
    private DecisionService decisionService;

    @Mock
    private ToolExecutionService toolExecutionService;

    @InjectMocks
    private RecoveryCaseProcessor processor;

    @Test
    void processNextCase_ShouldReturnFalse_WhenNoCasesAvailable() {
        when(recoveryCaseRepository.findNextAvailableCase()).thenReturn(Optional.empty());

        boolean result = processor.processNextCase();

        assertThat(result).isFalse();
        verifyNoInteractions(auditService);
    }

    @Test
    void processNextCase_ShouldProcessSuccessfully_WhenDependenciesReturnValidData() {
        RecoveryCase rc = new RecoveryCase();
        rc.setPaymentId("pay_123");
        rc.setStatus(CaseState.ELIGIBLE);

        Payment payment = new Payment();
        payment.setId("pay_123");

        AiDiagnosisResponse aiResponse = new AiDiagnosisResponse();
        aiResponse.setDiagnosis("INSUFFICIENT_FUNDS");
        aiResponse.setCandidateActions(List.of("SEND_EMAIL"));

        when(recoveryCaseRepository.findNextAvailableCase()).thenReturn(Optional.of(rc));
        when(paymentRepository.findById("pay_123")).thenReturn(Optional.of(payment));
        when(diagnosisService.diagnose(payment, rc)).thenReturn(aiResponse);
        when(auditEventRepository.findByCaseIdOrderBySequenceNoAsc(rc.getId())).thenReturn(List.of());
        PolicyEvaluationResult policyResult = new PolicyEvaluationResult(List.of(), List.of("SEND_EMAIL"));
        when(policyEngine.filterPermittedActions(eq(rc), eq(payment), eq(aiResponse.getCandidateActions()), anyList())).thenReturn(policyResult);
        
        DecisionResult decisionResult = new DecisionResult("SEND_EMAIL", new BigDecimal("500.00"));
        when(decisionService.selectBestAction(rc, payment, List.of("SEND_EMAIL"))).thenReturn(decisionResult);

        boolean result = processor.processNextCase();

        assertThat(result).isTrue();
        assertThat(rc.getStatus()).isEqualTo(CaseState.ACTION_EXECUTING);
        assertThat(rc.getChosenAction()).isEqualTo("SEND_EMAIL");
        verify(toolExecutionService).execute(rc, payment);
        verify(recoveryCaseRepository).save(rc);
    }

    @Test
    void processNextCase_ShouldPauseCase_WhenAiUnavailable() {
        UUID caseId = UUID.randomUUID();
        RecoveryCase rc = new RecoveryCase();
        rc.setId(caseId);
        rc.setPaymentId("pay_123");
        rc.setStatus(CaseState.ELIGIBLE);

        Payment payment = new Payment();
        payment.setId("pay_123");

        when(recoveryCaseRepository.findNextAvailableCase()).thenReturn(Optional.of(rc));
        when(paymentRepository.findById("pay_123")).thenReturn(Optional.of(payment));
        when(diagnosisService.diagnose(payment, rc)).thenThrow(new AiServiceUnavailableException("API DOWN"));

        boolean result = processor.processNextCase();

        assertThat(result).isTrue();
        // Stays in DIAGNOSING
        assertThat(rc.getStatus()).isEqualTo(CaseState.DIAGNOSING);
        assertThat(rc.getNextRunAt()).isNotNull(); // Has backoff

        verify(auditService).logEvent(eq(caseId), eq("STATE_CHANGED"), argThat(obj -> obj instanceof java.util.Map && ((java.util.Map<?,?>) obj).get("to") == CaseState.DIAGNOSING));
        verify(auditService).logEvent(eq(caseId), eq("AI_UNAVAILABLE"), argThat(obj -> obj instanceof java.util.Map && "API DOWN".equals(((java.util.Map<?,?>) obj).get("reason"))));
        
        verify(recoveryCaseRepository).save(rc);
    }
}
