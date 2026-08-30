package com.recoverai.integration.razorpay;

import com.recoverai.domain.payment.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentLinkServiceTest {

    @Mock
    private RazorpayClient razorpayClient;

    @InjectMocks
    private PaymentLinkService paymentLinkService;

    @Test
    void createPaymentLink_ShouldFormatPayloadAndCallClient() {
        Payment payment = new Payment();
        payment.setId("pay_123");
        payment.setAmount(new BigDecimal("1000.00"));

        String idempotencyKey = "case-1-RETRY-1";

        paymentLinkService.createPaymentLink(payment, idempotencyKey);

        verify(razorpayClient).createPaymentLink(
            argThat(payload -> {
                return payload.get("amount").equals(100000L) &&
                       payload.get("currency").equals("INR") &&
                       ((Map<?, ?>) payload.get("notes")).get("idempotency_key").equals(idempotencyKey);
            }),
            eq(idempotencyKey)
        );
    }
}
