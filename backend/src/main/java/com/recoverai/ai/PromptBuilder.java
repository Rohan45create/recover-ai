package com.recoverai.ai;

import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class PromptBuilder {

    public Map<String, Object> buildPromptPayload(Payment payment, RecoveryCase recoveryCase) {
        // Exclude PII! Only amount bucket, error code, method.
        Map<String, Object> features = new HashMap<>();
        
        features.put("amount_bucket", getAmountBucket(payment.getAmount()));
        features.put("error_code", payment.getErrorCode() != null ? payment.getErrorCode() : "UNKNOWN");
        features.put("payment_method", payment.getMethod() != null ? payment.getMethod() : "UNKNOWN");
        // We will assume 0 retries for now unless tracking explicitly in audit events
        features.put("retry_count", 0);
        
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a recovery diagnosis agent. Respond ONLY with JSON containing 'diagnosis', 'candidate_actions' (list of strings), and 'rationale'. " +
                "The 'candidate_actions' list MUST ONLY contain exact values from this set: [\"RETRY\", \"SEND_SMS\", \"SEND_WHATSAPP\", \"SEND_EMAIL\", \"SEND_REMINDER\", \"CREATE_PAYMENT_LINK\"].");
        
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "Features: " + features.toString());
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("messages", new Object[]{systemMessage, userMessage});
        // We can add model name later in NearAiClient
        
        return payload;
    }
    
    private String getAmountBucket(BigDecimal amount) {
        if (amount == null) return "UNKNOWN";
        if (amount.compareTo(new BigDecimal("1000")) < 0) {
            return "LOW";
        } else if (amount.compareTo(new BigDecimal("5000")) < 0) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }
}
