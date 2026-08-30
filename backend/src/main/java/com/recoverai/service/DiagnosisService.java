package com.recoverai.service;

import com.recoverai.ai.AiDiagnosisResponse;
import com.recoverai.ai.AiResponseValidator;
import com.recoverai.ai.NearAiClient;
import com.recoverai.ai.PromptBuilder;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final PromptBuilder promptBuilder;
    private final NearAiClient nearAiClient;
    private final AiResponseValidator aiResponseValidator;

    public AiDiagnosisResponse diagnose(Payment payment, RecoveryCase recoveryCase) {
        Map<String, Object> payload = promptBuilder.buildPromptPayload(payment, recoveryCase);
        String rawResponse = nearAiClient.getCompletion(payload);
        return aiResponseValidator.validateAndParse(rawResponse);
    }
}
