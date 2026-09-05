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
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
            @RequestHeader org.springframework.http.HttpHeaders headers) {

        // ===== STAGE 1: RAW ARRIVAL =====
        log.info("[WEBHOOK-STAGE-1] Hit /api/webhooks/razorpay | signature={} | headers={} | body={}",
                signature, headers.toSingleValueMap(), rawPayload);

        // ===== STAGE 2: SIGNATURE VERIFICATION =====
        if (signature == null) {
            log.info("[WEBHOOK-STAGE-2] FAIL — X-Razorpay-Signature header is missing entirely");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        boolean sigValid = signatureVerifier.verifySignature(rawPayload, signature);
        if (!sigValid) {
            log.info("[WEBHOOK-STAGE-2] FAIL — HMAC mismatch: provided={}", signature);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        log.info("[WEBHOOK-STAGE-2] PASS — signature verified OK");

        try {
            RazorpayWebhookPayload payload = objectMapper.readValue(rawPayload, RazorpayWebhookPayload.class);

            // ===== STAGE 3: PARSED FIELDS =====
            String paymentId = null;
            Object amount = null;
            String errorCode = null;
            String contact = null;
            String email = null;
            
            RazorpayWebhookPayload.Entity e = null;
            if (payload.getPayload() != null) {
                if ("payment.failed".equals(payload.getEvent()) && payload.getPayload().getPayment() != null) {
                    e = payload.getPayload().getPayment().getEntity();
                } else if ("payment.captured".equals(payload.getEvent()) && payload.getPayload().getPayment() != null) {
                    e = payload.getPayload().getPayment().getEntity();
                } else if ("payment_link.paid".equals(payload.getEvent()) && payload.getPayload().getPaymentLink() != null) {
                    e = payload.getPayload().getPaymentLink().getEntity();
                }
            }

            if (e != null) {
                paymentId = e.getId();
                
                // Razorpay sends amount in paise. Convert to rupees.
                if (e.getAmount() != null) {
                    e.setAmount(e.getAmount().divide(new java.math.BigDecimal("100")));
                }
                amount = e.getAmount();
                
                errorCode = e.getErrorCode();
                contact = e.getContact();
                email = e.getEmail();
            }
            log.info("[WEBHOOK-STAGE-3] Parsed: event={} payment_id={} amount={} error_code={} contact={} email={}",
                    payload.getEvent(), paymentId, amount, errorCode,
                    contact != null ? contact : "<none>",
                    email   != null ? email   : "<none>");

            if ("payment_link.paid".equals(payload.getEvent())) {
                ingestionService.processPaymentLinkPaid(payload);
            } else if ("payment.captured".equals(payload.getEvent())) {
                ingestionService.processPaymentCaptured(payload);
            } else if ("payment.failed".equals(payload.getEvent())) {
                ingestionService.processWebhook(payload);
            } else {
                log.info("[WEBHOOK-STAGE-3] Ignored unhandled event: {}", payload.getEvent());
            }
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("[WEBHOOK-STAGE-3] Failed to parse or process webhook payload", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
