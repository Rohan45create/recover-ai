package com.recoverai.service;

import com.recoverai.domain.policy.Policy;
import com.recoverai.domain.policy.PolicyAuditLog;
import com.recoverai.repository.PolicyAuditLogRepository;
import com.recoverai.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyAuditLogRepository policyAuditLogRepository;

    @Transactional(readOnly = true)
    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    @Transactional
    public Policy updatePolicy(String id, String newValue, Boolean newEnabled, String changedBy) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + id));

        // Capture old state for audit
        String  oldValue   = policy.getValue();
        Boolean oldEnabled = policy.getEnabled();

        // Apply changes (only update fields that were provided)
        if (newValue != null) {
            policy.setValue(newValue);
        }
        if (newEnabled != null) {
            policy.setEnabled(newEnabled);
        }
        policy.setUpdatedAt(OffsetDateTime.now());
        policy.setUpdatedBy(changedBy);

        policyRepository.save(policy);

        // Write audit log entry
        PolicyAuditLog auditEntry = PolicyAuditLog.builder()
                .id(UUID.randomUUID())
                .policyId(id)
                .oldValue(oldValue)
                .newValue(newValue != null ? newValue : oldValue)
                .oldEnabled(oldEnabled)
                .newEnabled(newEnabled != null ? newEnabled : oldEnabled)
                .changedBy(changedBy)
                .changedAt(OffsetDateTime.now())
                .build();
        policyAuditLogRepository.save(auditEntry);

        log.info("Policy {} updated by {}: value {} -> {}, enabled {} -> {}",
                id, changedBy, oldValue, policy.getValue(), oldEnabled, policy.getEnabled());

        return policy;
    }
}
