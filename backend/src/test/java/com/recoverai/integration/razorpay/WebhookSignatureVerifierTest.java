package com.recoverai.integration.razorpay;

import com.recoverai.config.RazorpayConfig;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookSignatureVerifierTest {

    @Test
    void shouldVerifyValidSignature() throws Exception {
        RazorpayConfig config = new RazorpayConfig();
        config.setWebhookSecret("my_secret");
        
        WebhookSignatureVerifier verifier = new WebhookSignatureVerifier(config);
        
        String payload = "{\"event\":\"payment.failed\"}";
        
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec("my_secret".getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        String validSignature = sb.toString();
        
        assertThat(verifier.verifySignature(payload, validSignature)).isTrue();
    }

    @Test
    void shouldRejectInvalidSignature() {
        RazorpayConfig config = new RazorpayConfig();
        config.setWebhookSecret("my_secret");
        
        WebhookSignatureVerifier verifier = new WebhookSignatureVerifier(config);
        
        String payload = "{\"event\":\"payment.failed\"}";
        String invalidSignature = "invalid123";
        
        assertThat(verifier.verifySignature(payload, invalidSignature)).isFalse();
    }
}
