package com.recoverai.integration.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationSender {

    /**
     * Dispatches a reminder notification to the customer.
     *
     * @param channel        notification type (SEND_REMINDER, SEND_SMS, SEND_WHATSAPP, SEND_EMAIL)
     * @param customerId     internal customer/payment ID for traceability
     * @param idempotencyKey dedup key
     * @param contact        customer phone number (may be null)
     * @param email          customer email address (may be null)
     */
    public void send(String channel, String customerId, String idempotencyKey, String contact, String email) {
        log.info("Sending notification via channel='{}' to customerId='{}', idempotency_key='{}', contact={}, email={}",
                 channel, customerId, idempotencyKey,
                 contact != null ? contact : "<none>",
                 email   != null ? email   : "<none>");

        // In a real system, we would dispatch to Twilio (SMS), Gupshup (WhatsApp), or an email provider here.
        // The actual delivery depends on which fields are present:
        if (contact == null && email == null) {
            log.warn("NotificationSender called with no contact info for customerId={} — skipping dispatch", customerId);
        }
    }
}
