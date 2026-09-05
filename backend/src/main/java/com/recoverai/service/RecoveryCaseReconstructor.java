package com.recoverai.service;

import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.recovery.CaseState;
import com.recoverai.domain.recovery.RecoveryCase;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RecoveryCaseReconstructor {

    private final ObjectMapper objectMapper;

    public RecoveryCaseReconstructor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RecoveryCase reconstruct(UUID caseId, List<AuditEvent> events) {
        if (events == null || events.isEmpty()) {
            return null;
        }

        RecoveryCase rc = new RecoveryCase();
        rc.setId(caseId);

        for (AuditEvent event : events) {
            applyEvent(rc, event);
        }

        return rc;
    }

    private void applyEvent(RecoveryCase rc, AuditEvent event) {
        try {
            JsonNode payload = objectMapper.readTree(event.getPayload());
            String eventType = event.getEventType();

            switch (eventType) {
                case "RECEIVED":
                    rc.setCreatedAt(event.getCreatedAt());
                    break;
                case "STATE_CHANGED":
                    if (payload.has("to")) {
                        rc.setStatus(CaseState.valueOf(payload.get("to").asText()));
                    }
                    if (payload.has("chosen_action")) {
                        rc.setChosenAction(payload.get("chosen_action").asText());
                    }
                    JsonNode expectedValue = payload.get("expected_value");
                    if (expectedValue != null) {
                        rc.setExpectedRecoveryValue(new java.math.BigDecimal(expectedValue.asText()));
                    }
                    if (payload.has("next_run_at")) {
                        rc.setNextRunAt(OffsetDateTime.parse(payload.get("next_run_at").asText()));
                    }
                    break;
                case "ACTION_EXECUTED":
                case "ACTION_FAILED":
                case "DIAGNOSIS_COMPLETED":
                    // Not modifying the RecoveryCase directly, handled by STATE_CHANGED usually
                    break;
                default:
                    // Unknown event, skip
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process event " + event.getId(), e);
        }
    }
}
