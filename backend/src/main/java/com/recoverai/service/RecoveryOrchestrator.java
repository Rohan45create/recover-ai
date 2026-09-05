package com.recoverai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoveryOrchestrator {

    private final RecoveryCaseProcessor processor;
    private final com.recoverai.repository.RecoveryCaseRepository recoveryCaseRepository;
    private final AuditService auditService;

    @Scheduled(fixedDelayString = "5000")
    public void pollAndProcess() {
        boolean hasMore = true;
        int processedCount = 0;

        while (hasMore) {
            try {
                boolean processed = processor.processNextCase();
                if (processed) {
                    processedCount++;
                    // STAGE 5 logging happens inside RecoveryCaseProcessor per case
                }
                hasMore = processed;
            } catch (Exception e) {
                log.error("[ORCHESTRATOR-STAGE-5] Error processing recovery case", e);
                hasMore = false;
            }
        }

        if (processedCount > 0) {
            log.info("[ORCHESTRATOR-STAGE-5] Finished poll cycle, processed {} case(s).", processedCount);
        }
    }

    @Scheduled(fixedDelayString = "60000") // Run every minute
    public void sweepWaitingCases() {
        // Find cases in WAITING state that haven't been updated in 72 hours
        java.time.OffsetDateTime threshold = java.time.OffsetDateTime.now().minusHours(72);
        java.util.List<com.recoverai.domain.recovery.RecoveryCase> expiredCases = 
            recoveryCaseRepository.findByStatusAndUpdatedAtBefore(com.recoverai.domain.recovery.CaseState.WAITING, threshold);
        
        for (com.recoverai.domain.recovery.RecoveryCase rc : expiredCases) {
            rc.setStatus(com.recoverai.domain.recovery.CaseState.STOPPED);
            rc.setUpdatedAt(java.time.OffsetDateTime.now());
            recoveryCaseRepository.save(rc);

            String action = rc.getChosenAction() != null ? rc.getChosenAction() : "unknown action";
            auditService.logEvent(rc.getId(), "OUTCOME", java.util.Map.of(
                    "status", "STOPPED",
                    "reason", String.format(
                            "Recovery window expired with no customer response. " +
                            "Case was waiting on %s. Case stopped after 72h timeout.", action)
            ));
            log.info("[ORCHESTRATOR-TIMEOUT] Case id={} chosenAction={} marked STOPPED (72h timeout — no customer response)", rc.getId(), action);
        }
    }
}
