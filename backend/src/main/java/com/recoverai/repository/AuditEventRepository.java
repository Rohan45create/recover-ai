package com.recoverai.repository;

import com.recoverai.domain.audit.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    List<AuditEvent> findByCaseIdOrderBySequenceNoAsc(UUID caseId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(MAX(a.sequenceNo), 0) FROM AuditEvent a WHERE a.caseId = :caseId")
    Integer findMaxSequenceNoByCaseId(@org.springframework.data.repository.query.Param("caseId") UUID caseId);

    long countByEventType(String eventType);

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM audit_events WHERE event_type = 'POLICY_EVALUATION' AND CAST(payload AS TEXT) LIKE '%\"outcome\":\"BLOCKED\"%'", nativeQuery = true)
    long countBlockedPolicyEvaluations();
}
