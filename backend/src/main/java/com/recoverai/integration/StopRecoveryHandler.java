package com.recoverai.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StopRecoveryHandler {

    public void stop(String caseId, String idempotencyKey) {
        log.info("Stopping recovery for case '{}', idempotency_key='{}'", caseId, idempotencyKey);
    }
}
