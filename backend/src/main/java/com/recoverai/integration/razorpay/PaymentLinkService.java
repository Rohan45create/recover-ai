package com.recoverai.integration.razorpay;

import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
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

    public String createPaymentLink(RecoveryCase rc, Payment payment, String idempotencyKey) {
        log.info("Creating Payment Link for payment {} (case {}) with idempotencyKey {}", payment.getId(), rc.getId(), idempotencyKey);
        
        Map<String, Object> payload = new HashMap<>();
        // Amount is typically in paise (amount * 100)
        payload.put("amount", payment.getAmount().multiply(new java.math.BigDecimal("100")).longValue());
        payload.put("currency", "INR");
        payload.put("accept_partial", false);
        payload.put("description", "Retry payment for " + payment.getId());
        
        // Build customer map from real captured contact info (nullable — guard each field)
        Map<String, Object> customer = new HashMap<>();
        customer.put("name", "Customer");
        if (payment.getContact() != null && !payment.getContact().isBlank()) {
            customer.put("contact", payment.getContact());
        }
        if (payment.getEmail() != null && !payment.getEmail().isBlank()) {
            customer.put("email", payment.getEmail());
        }
        payload.put("customer", customer);

        // Only notify via channels where we actually have contact info
        Map<String, Object> notify = new HashMap<>();
        notify.put("sms",   payment.getContact() != null && !payment.getContact().isBlank());
        notify.put("email", payment.getEmail()   != null && !payment.getEmail().isBlank());
        payload.put("notify", notify);
        payload.put("reminder_enable", true);
        
        log.info("[PAYMENT-LINK] Sending to Razorpay: caseId={} notify.email={} notify.sms={} email={} contact={}",
                 rc.getId(), notify.get("email"), notify.get("sms"), payment.getEmail(), payment.getContact());
        
        // Pass idempotency key inside notes as well for traceability on Razorpay dashboard
        Map<String, Object> notes = new HashMap<>();
        notes.put("idempotency_key", idempotencyKey);
        notes.put("original_payment_id", payment.getId());
        notes.put("case_id", rc.getId().toString());
        payload.put("notes", notes);

        // Execute API call via client
        Map<String, Object> response = razorpayClient.createPaymentLink(payload, idempotencyKey);
        if (response != null) {
            String shortUrl = (String) response.get("short_url");
            // === DISTINCT GREPPABLE LOG — search for [PAYMENT-LINK-URL] to find all created links ===
            log.info("[PAYMENT-LINK-URL] case={} short_url={}", rc.getId(), shortUrl);
            log.info("Razorpay Payment Link created: short_url={} id={}", shortUrl, response.get("id"));
            return shortUrl;
        }
        return null;
    }
}
