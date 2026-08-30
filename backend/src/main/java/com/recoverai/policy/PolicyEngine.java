package com.recoverai.policy;

import com.recoverai.domain.audit.AuditEvent;
import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyEngine {

    private final List<PolicyRule> rules;

    public PolicyEvaluationResult filterPermittedActions(RecoveryCase rc, Payment payment, List<String> candidateActions, List<AuditEvent> history) {
        List<String> permitted = new ArrayList<>();
        List<PolicyEvaluationResult.ActionEvaluation> evaluations = new ArrayList<>();
        
        for (String action : candidateActions) {
            boolean actionPermitted = true;
            String blockReason = null;
            for (PolicyRule rule : rules) {
                if (!rule.isPermitted(rc, payment, action, history)) {
                    blockReason = rule.getReason();
                    log.info("Action {} blocked for case {} due to: {}", action, rc.getId(), blockReason);
                    actionPermitted = false;
                    break;
                }
            }
            
            if (actionPermitted) {
                permitted.add(action);
                evaluations.add(new PolicyEvaluationResult.ActionEvaluation(action, true, "Passed all rules"));
            } else {
                evaluations.add(new PolicyEvaluationResult.ActionEvaluation(action, false, blockReason));
            }
        }
        
        return new PolicyEvaluationResult(evaluations, permitted);
    }
}
