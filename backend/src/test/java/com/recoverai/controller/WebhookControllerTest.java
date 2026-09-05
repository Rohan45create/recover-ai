package com.recoverai.controller;

import tools.jackson.databind.ObjectMapper;
import com.recoverai.dto.RazorpayWebhookPayload;
import com.recoverai.integration.razorpay.WebhookSignatureVerifier;
import com.recoverai.service.IngestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IngestionService ingestionService;

    @Mock
    private WebhookSignatureVerifier signatureVerifier;
    
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WebhookController webhookController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(webhookController).build();
    }

    @Test
    void handleRazorpayWebhook_ShouldReturn200AndProcessWebhook() throws Exception {
        String payloadJson = """
            {
              "event": "payment.failed",
              "payload": {
                "payment": {
                  "entity": {
                    "id": "pay_123",
                    "amount": 500000,
                    "currency": "INR",
                    "status": "failed",
                    "error_code": "BAD_REQUEST_ERROR"
                  }
                }
              }
            }
            """;

        RazorpayWebhookPayload capturedPayload = new RazorpayWebhookPayload();
        RazorpayWebhookPayload.Payload payloadWrapper = new RazorpayWebhookPayload.Payload();
        RazorpayWebhookPayload.PaymentEntity paymentEntity = new RazorpayWebhookPayload.PaymentEntity();
        RazorpayWebhookPayload.Entity entity = new RazorpayWebhookPayload.Entity();
        entity.setId("pay_123");
        entity.setAmount(new java.math.BigDecimal("500000")); // parsed amount before controller manipulates it
        paymentEntity.setEntity(entity);
        payloadWrapper.setPayment(paymentEntity);
        capturedPayload.setPayload(payloadWrapper);
        capturedPayload.setEvent("payment.failed");

        when(signatureVerifier.verifySignature(anyString(), eq("valid_sig"))).thenReturn(true);
        when(objectMapper.readValue(anyString(), eq(RazorpayWebhookPayload.class))).thenReturn(capturedPayload);

        mockMvc.perform(post("/api/webhooks/razorpay")
                .header("X-Razorpay-Signature", "valid_sig")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isOk());

        verify(ingestionService).processWebhook(any(RazorpayWebhookPayload.class));
        org.junit.jupiter.api.Assertions.assertEquals(new java.math.BigDecimal("5000.00").compareTo(capturedPayload.getPayload().getPayment().getEntity().getAmount()), 0);
    }
    
    @Test
    void handleRazorpayWebhook_ShouldReturn400WhenSignatureInvalid() throws Exception {
        String payloadJson = "{}";

        when(signatureVerifier.verifySignature(anyString(), eq("invalid_sig"))).thenReturn(false);

        mockMvc.perform(post("/api/webhooks/razorpay")
                .header("X-Razorpay-Signature", "invalid_sig")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isBadRequest());
    }
}

