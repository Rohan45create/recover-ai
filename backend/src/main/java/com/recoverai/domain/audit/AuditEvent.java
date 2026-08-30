package com.recoverai.domain.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "case_id", nullable = false, updatable = false)
    private UUID caseId;

    @Column(name = "event_type", nullable = false, updatable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, updatable = false)
    private String payload;

    @Column(name = "sequence_no", nullable = false, updatable = false)
    private Integer sequenceNo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
