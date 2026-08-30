package com.recoverai.service;

import tools.jackson.databind.ObjectMapper;
import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void logEvent(UUID caseId, String eventType, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            int currentCount = auditEventRepository.findMaxSequenceNoByCaseId(caseId);
            int sequenceNo = currentCount + 1;

            AuditEvent event = AuditEvent.builder()
                    .id(UUID.randomUUID())
                    .caseId(caseId)
                    .eventType(eventType)
                    .payload(jsonPayload)
                    .sequenceNo(sequenceNo)
                    .createdAt(OffsetDateTime.now())
                    .build();

            auditEventRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize audit payload", e);
        }
    }
}
