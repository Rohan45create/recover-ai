package com.recoverai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RazorpayWebhookPayload {

    private String event;
    private Payload payload;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        private PaymentEntity payment;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentEntity {
        private Entity entity;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entity {
        private String id;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String method;
        @JsonProperty("customer_id")
        private String customerId;
        private String contact;
        @JsonProperty("error_code")
        private String errorCode;
        @JsonProperty("error_description")
        private String errorDescription;
    }
}
