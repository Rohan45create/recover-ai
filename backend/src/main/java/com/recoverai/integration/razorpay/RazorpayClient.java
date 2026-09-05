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
        // Log key prefix for credential diagnostic — never log the full secret
        String keyPrefix = config.getKeyId() != null && config.getKeyId().length() > 8
                ? config.getKeyId().substring(0, 8) + "..."
                : config.getKeyId();
        log.info("[RAZORPAY-CLIENT] Creating payment link using keyId prefix={}", keyPrefix);

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

    public Map<String, Object> createOrder(Map<String, Object> payload) {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                (config.getKeyId() + ":" + config.getKeySecret()).getBytes()
        );

        try {
            return restClient.post()
                    .uri("/orders")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
        } catch (HttpClientErrorException e) {
            log.error("Failed to create Razorpay Order: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("Razorpay API Error: " + e.getStatusCode(), e);
        }
    }
}
