package com.recoverai.ai;

import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class AiResponseValidator {

    private final ObjectMapper objectMapper;
    
    public AiResponseValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public AiDiagnosisResponse validateAndParse(String jsonResponse) {
        try {
            // Very simple parser for the JSON response
            // AI responses might be wrapped in markdown code blocks like ```json ... ```
            String cleanJson = jsonResponse.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            
            cleanJson = cleanJson.trim();
            return objectMapper.readValue(cleanJson, AiDiagnosisResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response: " + jsonResponse, e);
        }
    }
}
