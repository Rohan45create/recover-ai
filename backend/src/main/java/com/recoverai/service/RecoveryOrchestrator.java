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

    @Scheduled(fixedDelayString = "5000")
    public void pollAndProcess() {
        boolean hasMore = true;
        int processedCount = 0;

        while (hasMore) {
            try {
                hasMore = processor.processNextCase();
                if (hasMore) {
                    processedCount++;
                }
            } catch (Exception e) {
                log.error("Error processing recovery case", e);
                // On error, we stop pulling for this loop to prevent infinite error loops.
                // The next schedule tick will retry fetching cases.
                hasMore = false;
            }
        }

        if (processedCount > 0) {
            log.info("RecoveryOrchestrator finished polling, processed {} cases.", processedCount);
        }
    }
}
