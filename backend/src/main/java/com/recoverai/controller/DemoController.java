package com.recoverai.controller;

import com.recoverai.integration.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Profile("!test")
public class DemoController {

    private final RazorpayClient razorpayClient;

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createDemoOrder(@RequestBody Map<String, Object> request) {
        try {
            Number amountRaw = (Number) request.get("amount");
            if (amountRaw == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount is required"));
            }
            
            long amountInPaise = amountRaw.longValue() * 100;

            // Optional customer contact fields for live demo — Razorpay will pre-fill checkout
            String email = request.containsKey("email") ? (String) request.get("email") : null;
            String phone = request.containsKey("phone") ? (String) request.get("phone") : null;

            // Include contact info in notes so it surfaces in Razorpay dashboard and webhook payload
            Map<String, Object> notes = new HashMap<>();
            if (email != null && !email.isBlank()) {
                notes.put("demo_email", email);
            }
            if (phone != null && !phone.isBlank()) {
                notes.put("demo_phone", phone);
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", amountInPaise);
            payload.put("currency", "INR");
            payload.put("receipt", "receipt_demo_" + System.currentTimeMillis());
            if (!notes.isEmpty()) {
                payload.put("notes", notes);
            }

            Map<String, Object> order = razorpayClient.createOrder(payload);
            
            // Return safe fields to the frontend including echo of contact details for prefill
            Map<String, Object> safeResponse = new HashMap<>();
            safeResponse.put("id", order.get("id"));
            safeResponse.put("amount", order.get("amount"));
            safeResponse.put("currency", order.get("currency"));
            if (email != null && !email.isBlank()) safeResponse.put("email", email);
            if (phone != null && !phone.isBlank()) safeResponse.put("phone", phone);

            return ResponseEntity.ok(safeResponse);
            
        } catch (Exception e) {
            log.error("Failed to create demo order", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
