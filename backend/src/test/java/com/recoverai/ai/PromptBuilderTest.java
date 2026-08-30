package com.recoverai.ai;

import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderTest {

    private final PromptBuilder promptBuilder = new PromptBuilder();

    @Test
    void buildPromptPayload_ShouldExcludePII_AndCalculateAmountBucket() {
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("1500"));
        payment.setErrorCode("BAD_REQUEST");
        payment.setMethod("UPI");
        // These are PII and should be omitted
        payment.setCustomerId("cust_123");
        payment.setContact("rohan@example.com");

        RecoveryCase rc = new RecoveryCase();

        Map<String, Object> payload = promptBuilder.buildPromptPayload(payment, rc);
        
        Object[] messages = (Object[]) payload.get("messages");
        assertThat(messages).hasSize(2);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> userMessage = (Map<String, Object>) messages[1];
        String content = (String) userMessage.get("content");
        
        // Ensure bucket is calculated (1500 -> MEDIUM)
        assertThat(content).contains("amount_bucket=MEDIUM");
        assertThat(content).contains("error_code=BAD_REQUEST");
        assertThat(content).contains("payment_method=UPI");
        
        // Ensure PII is excluded
        assertThat(content).doesNotContain("cust_123");
        assertThat(content).doesNotContain("rohan@example.com");
    }
}
