package com.recoverai.domain.policy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "policy_audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyAuditLog {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "policy_id", nullable = false, updatable = false, length = 20)
    private String policyId;

    @Column(name = "old_value", length = 100)
    private String oldValue;

    @Column(name = "new_value", length = 100)
    private String newValue;

    @Column(name = "old_enabled")
    private Boolean oldEnabled;

    @Column(name = "new_enabled")
    private Boolean newEnabled;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private OffsetDateTime changedAt;
}
