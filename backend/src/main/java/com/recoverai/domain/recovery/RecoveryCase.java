package com.recoverai.domain.recovery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "recovery_cases")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecoveryCase {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "payment_id", nullable = false, updatable = false)
    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CaseState status;

    @Column(length = 255)
    private String diagnosis;

    @Column(name = "chosen_action", length = 255)
    private String chosenAction;

    @Column(name = "expected_recovery_value")
    private BigDecimal expectedRecoveryValue;

    @Column(name = "actual_recovered_amount")
    private BigDecimal actualRecoveredAmount;

    @Column(name = "payment_link_url", length = 512)
    private String paymentLinkUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "next_run_at")
    private OffsetDateTime nextRunAt;
}
