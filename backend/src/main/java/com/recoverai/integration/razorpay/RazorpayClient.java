package com.recoverai.integration.razorpay;

import com.recoverai.config.RazorpayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RazorpayClient {

    private final RazorpayConfig config;
    private final RestClient restClient = RestClient.create("https://api.razorpay.com/v1");

    public Map<String, Object> createPaymentLink(Map<String, Object> payload, String idempotencyKey) {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                (config.getKeyId() + ":" + config.getKeySecret()).getBytes()
        );

        try {
            return restClient.post()
                    .uri("/payment_links")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
        } catch (HttpClientErrorException e) {
            log.error("Failed to create Razorpay Payment Link: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("Razorpay API Error: " + e.getStatusCode(), e);
        }
    }
}
