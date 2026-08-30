package com.recoverai.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EscalationService {

    public void escalate(String caseId, String idempotencyKey) {
        log.info("Escalating case '{}', idempotency_key='{}'", caseId, idempotencyKey);
        // Could integrate with Zendesk or JIRA
    }
}
