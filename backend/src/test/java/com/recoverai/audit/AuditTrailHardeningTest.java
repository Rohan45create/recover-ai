package com.recoverai.audit;

import com.recoverai.TestcontainersConfiguration;
import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.recovery.CaseState;
import com.recoverai.domain.recovery.RecoveryCase;
import com.recoverai.repository.AuditEventRepository;
import com.recoverai.service.AuditService;
import com.recoverai.service.RecoveryCaseReconstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test") // Use a test profile if needed, or default
class AuditTrailHardeningTest {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RecoveryCaseReconstructor reconstructor;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldPreventUpdateAndDeleteOnAuditEvents() {
        UUID caseId = UUID.randomUUID();
        
        // 1. Insert an audit event
        auditService.logEvent(caseId, "TEST_EVENT", Map.of("key", "value"));

        List<AuditEvent> events = auditEventRepository.findByCaseIdOrderBySequenceNoAsc(caseId);
        assertThat(events).hasSize(1);
        UUID eventId = events.get(0).getId();

        // 2. Attempt UPDATE (should fail)
        assertThatThrownBy(() -> {
            jdbcTemplate.update("UPDATE audit_events SET event_type = 'TAMPERED' WHERE id = ?", eventId);
        }).hasMessageContaining("audit_events is append-only");

        // 3. Attempt DELETE (should fail)
        assertThatThrownBy(() -> {
            jdbcTemplate.update("DELETE FROM audit_events WHERE id = ?", eventId);
        }).hasMessageContaining("audit_events is append-only");
    }

    @Test
    void shouldReconstructRecoveryCaseFromAuditTrail() {
        UUID caseId = UUID.randomUUID();
        
        // Simulate lifecycle
        auditService.logEvent(caseId, "RECEIVED", Map.of("payment_id", "pay_123"));
        auditService.logEvent(caseId, "STATE_CHANGED", Map.of(
            "to", "DIAGNOSING"
        ));
        auditService.logEvent(caseId, "DIAGNOSIS_COMPLETED", Map.of(
            "diagnosis", List.of("LOW_FUNDS")
        ));
        auditService.logEvent(caseId, "STATE_CHANGED", Map.of(
            "to", "ACTION_PENDING",
            "chosen_action", "RETRY",
            "expected_value", "250.00",
            "model_version", "gemma-4-31b"
        ));

        // Retrieve all events
        List<AuditEvent> events = auditEventRepository.findByCaseIdOrderBySequenceNoAsc(caseId);
        
        // Reconstruct
        RecoveryCase reconstructed = reconstructor.reconstruct(caseId, events);

        // Verify
        assertThat(reconstructed).isNotNull();
        assertThat(reconstructed.getId()).isEqualTo(caseId);
        assertThat(reconstructed.getStatus()).isEqualTo(CaseState.ACTION_PENDING);
        assertThat(reconstructed.getChosenAction()).isEqualTo("RETRY");
        assertThat(reconstructed.getExpectedRecoveryValue()).isEqualByComparingTo("250.00");
        assertThat(reconstructed.getCreatedAt()).isNotNull();
    }
}
