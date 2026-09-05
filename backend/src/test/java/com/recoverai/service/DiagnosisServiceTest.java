package com.recoverai.service;

import com.recoverai.ai.AiDiagnosisResponse;
import com.recoverai.ai.AiResponseValidator;
import com.recoverai.ai.GroqClient;
import com.recoverai.ai.NearAiClient;
import com.recoverai.ai.PromptBuilder;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosisServiceTest {

    @Mock
    private PromptBuilder promptBuilder;
    @Mock
    private NearAiClient nearAiClient;
    @Mock
    private GroqClient groqClient;
    @Mock
    private AiResponseValidator aiResponseValidator;

    @InjectMocks
    private DiagnosisService diagnosisService;

    @Test
    void diagnose_ShouldUseGroqAsPrimaryAndReturnGroqFallbackProvider() {
        // Groq is now the primary path — happy path uses Groq
        Payment p = new Payment();
        RecoveryCase rc = new RecoveryCase();
        Map<String, Object> payload = Map.of("messages", "test");
        AiDiagnosisResponse aiResponse = new AiDiagnosisResponse();
        aiResponse.setDiagnosis("INSUFFICIENT_FUNDS");

        when(promptBuilder.buildPromptPayload(p, rc)).thenReturn(payload);
        when(groqClient.getCompletion(payload)).thenReturn("```json...```");
        when(aiResponseValidator.validateAndParse("```json...```")).thenReturn(aiResponse);

        AiDiagnosisResponse result = diagnosisService.diagnose(p, rc);

        assertThat(result.getDiagnosis()).isEqualTo("INSUFFICIENT_FUNDS");
        // Primary (Groq) succeeded → provider is GROQ_FALLBACK
        assertThat(result.getAiProvider()).isEqualTo("GROQ_FALLBACK");
    }

    @Test
    void diagnose_ShouldFallbackToNearAiWhenGroqFails() {
        // When Groq (primary) fails, NearAI (secondary) is attempted → provider is NEAR_AI
        Payment p = new Payment();
        RecoveryCase rc = new RecoveryCase();
        Map<String, Object> payload = Map.of("messages", "test");
        AiDiagnosisResponse aiResponse = new AiDiagnosisResponse();
        aiResponse.setDiagnosis("FRAUD_SUSPECTED");

        when(promptBuilder.buildPromptPayload(p, rc)).thenReturn(payload);
        when(groqClient.getCompletion(payload)).thenThrow(new RuntimeException("Groq down"));
        when(nearAiClient.getCompletion(payload)).thenReturn("```json_from_nearai```");
        when(aiResponseValidator.validateAndParse("```json_from_nearai```")).thenReturn(aiResponse);

        AiDiagnosisResponse result = diagnosisService.diagnose(p, rc);

        assertThat(result.getDiagnosis()).isEqualTo("FRAUD_SUSPECTED");
        // Fallback (NearAI) succeeded → provider is NEAR_AI (genuine TEE response)
        assertThat(result.getAiProvider()).isEqualTo("NEAR_AI");
    }
}
