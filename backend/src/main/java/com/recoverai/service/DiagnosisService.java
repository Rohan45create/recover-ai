package com.recoverai.service;

import com.recoverai.ai.AiDiagnosisResponse;
import com.recoverai.ai.AiResponseValidator;
import com.recoverai.ai.GroqClient;
import com.recoverai.ai.NearAiClient;
import com.recoverai.ai.PromptBuilder;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final PromptBuilder promptBuilder;
    private final NearAiClient nearAiClient;
    private final GroqClient groqClient;
    private final AiResponseValidator aiResponseValidator;

    public AiDiagnosisResponse diagnose(Payment payment, RecoveryCase recoveryCase) {
        Map<String, Object> payload = promptBuilder.buildPromptPayload(payment, recoveryCase);
        
        String rawResponse;
        String provider;

        // Groq is the primary path (NearAI confidential completions endpoint is being re-validated).
        // NearAI is kept as the secondary attempt so it exercises the real endpoint whenever it comes up.
        try {
            rawResponse = groqClient.getCompletion(payload);
            provider = "GROQ_FALLBACK";
        } catch (Exception groqEx) {
            log.warn("GroqClient failed (primary), falling back to NearAiClient: {}", groqEx.getMessage());
            try {
                rawResponse = nearAiClient.getCompletion(payload);
                provider = "NEAR_AI";
            } catch (Exception nearEx) {
                log.error("Both AI providers failed — Groq: {} | NearAI: {}", groqEx.getMessage(), nearEx.getMessage());
                throw new AiServiceUnavailableException("All AI providers unavailable", nearEx);
            }
        }

        AiDiagnosisResponse response = aiResponseValidator.validateAndParse(rawResponse);
        response.setAiProvider(provider);

        // ===== STAGE 6: PROVIDER + RAW RESPONSE =====
        log.info("[DIAGNOSIS-STAGE-6] Provider={} rawResponse={}", provider, rawResponse);

        return response;
    }
}
