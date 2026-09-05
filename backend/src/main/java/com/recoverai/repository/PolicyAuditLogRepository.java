package com.recoverai.repository;

import com.recoverai.domain.policy.PolicyAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PolicyAuditLogRepository extends JpaRepository<PolicyAuditLog, UUID> {
}
