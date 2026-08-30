package com.recoverai.domain.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(length = 50)
    private String method;

    @Column(name = "customer_id")
    private String customerId;

    @Column(length = 50)
    private String contact;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_description")
    private String errorDescription;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
