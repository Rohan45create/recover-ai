package com.recoverai.ai;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.HashMap;

@Component
public class GroqClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public GroqClient(
            @Value("${groq.key:}") String apiKey,
            @Value("${groq.url:https://api.groq.com/openai/v1/chat/completions}") String baseUrl,
            @Value("${groq.model:openai/gpt-oss-120b}") String model,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String getCompletion(Map<String, Object> payload) {
        try {
            // Add model to payload if not present or overwrite
            Map<String, Object> groqPayload = new HashMap<>(payload);
            groqPayload.put("model", model);
            
            String responseBody = restClient.post()
                    .body(groqPayload)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode choices = rootNode.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode message = choices.get(0).path("message");
                return message.path("content").asText();
            }
            throw new Exception("Invalid response format from Groq");
        } catch (Exception e) {
            System.err.println("Groq fallback call failed: " + e.getMessage());
            throw new RuntimeException("Groq API Error: " + e.getMessage(), e);
        }
    }
}
