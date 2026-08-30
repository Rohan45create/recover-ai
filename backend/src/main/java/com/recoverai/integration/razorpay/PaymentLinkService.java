package com.recoverai.integration.razorpay;

import com.recoverai.domain.payment.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentLinkService {

    private final RazorpayClient razorpayClient;

    public void createPaymentLink(Payment payment, String idempotencyKey) {
        log.info("Creating Payment Link for payment {} with idempotencyKey {}", payment.getId(), idempotencyKey);
        
        Map<String, Object> payload = new HashMap<>();
        // Amount is typically in paise (amount * 100)
        payload.put("amount", payment.getAmount().multiply(new java.math.BigDecimal("100")).longValue());
        payload.put("currency", "INR");
        payload.put("accept_partial", false);
        payload.put("description", "Retry payment for " + payment.getId());
        
        Map<String, Object> customer = new HashMap<>();
        customer.put("name", "Customer");
        // In real app we would use customer details from DB, stubbing here
        customer.put("contact", "+919999999999");
        customer.put("email", "customer@example.com");
        payload.put("customer", customer);
        
        payload.put("notify", Map.of("sms", true, "email", true));
        payload.put("reminder_enable", true);
        
        // Pass idempotency key inside notes as well for traceability on Razorpay dashboard
        Map<String, Object> notes = new HashMap<>();
        notes.put("idempotency_key", idempotencyKey);
        notes.put("original_payment_id", payment.getId());
        payload.put("notes", notes);

        // Execute API call via client
        razorpayClient.createPaymentLink(payload, idempotencyKey);
    }
}
