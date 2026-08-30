package com.recoverai.integration.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationSender {

    public void send(String channel, String customerId, String idempotencyKey) {
        log.info("Sending notification via channel '{}' to customer '{}', idempotency_key='{}'", 
                 channel, customerId, idempotencyKey);
        
        // Simulates network delay or API call
        // In a real system, we might use Twilio or Gupshup for SMS/WhatsApp
    }
}
