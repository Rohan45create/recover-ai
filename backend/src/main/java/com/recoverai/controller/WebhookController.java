package com.recoverai.controller;

import tools.jackson.databind.ObjectMapper;
import com.recoverai.dto.RazorpayWebhookPayload;
import com.recoverai.integration.razorpay.WebhookSignatureVerifier;
import com.recoverai.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final IngestionService ingestionService;
    private final WebhookSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;

    @PostMapping("/razorpay")
    public ResponseEntity<Void> handleRazorpayWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        
        if (signature == null || !signatureVerifier.verifySignature(rawPayload, signature)) {
            log.warn("Invalid webhook signature received");
            // Audit service could be called here to log rejection, but we don't have a Case ID yet.
            // Returning 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            RazorpayWebhookPayload payload = objectMapper.readValue(rawPayload, RazorpayWebhookPayload.class);
            ingestionService.processWebhook(payload);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to parse webhook payload", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
