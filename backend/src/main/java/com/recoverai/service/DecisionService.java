package com.recoverai.service;

import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DecisionService {

    public DecisionResult selectBestAction(RecoveryCase rc, Payment payment, List<String> permittedActions) {
        if (permittedActions == null || permittedActions.isEmpty()) {
            return new DecisionResult("NONE", BigDecimal.ZERO);
        }

        String bestAction = "NONE";
        BigDecimal bestEV = new BigDecimal("-1");

        for (String action : permittedActions) {
            BigDecimal pRecovery = getRecoveryProbability(rc.getDiagnosis(), action);
            BigDecimal cost = getActionCost(action);
            
            // EV = (Amount * P) - Cost
            BigDecimal ev = payment.getAmount().multiply(pRecovery).subtract(cost);
            
            if (ev.compareTo(bestEV) > 0) {
                bestEV = ev;
                bestAction = action;
            }
        }
        
        // If the best expected value is not profitable, we shouldn't act
        if (bestEV.compareTo(BigDecimal.ZERO) <= 0) {
            return new DecisionResult("NONE", BigDecimal.ZERO);
        }

        return new DecisionResult(bestAction, bestEV);
    }

    private BigDecimal getRecoveryProbability(String diagnosis, String action) {
        // Hand-specified lookup table per architecture specs.
        // In real life this would be a DB table or a small model.
        if (diagnosis == null) return new BigDecimal("0.05");

        return switch (diagnosis) {
            case "INSUFFICIENT_FUNDS" -> switch (action) {
                case "RETRY" -> new BigDecimal("0.40");
                case "SEND_SMS" -> new BigDecimal("0.15");
                case "SEND_WHATSAPP" -> new BigDecimal("0.20");
                default -> new BigDecimal("0.05");
            };
            case "TEMPORARY_SYSTEM_FAILURE" -> switch (action) {
                case "RETRY" -> new BigDecimal("0.80");
                default -> new BigDecimal("0.10");
            };
            case "FRAUD_SUSPECTED" -> new BigDecimal("0.00"); // Never try to recover fraud
            default -> switch (action) {
                case "RETRY" -> new BigDecimal("0.10");
                case "SEND_EMAIL" -> new BigDecimal("0.10");
                default -> new BigDecimal("0.05");
            };
        };
    }

    private BigDecimal getActionCost(String action) {
        return switch (action) {
            case "SEND_SMS" -> new BigDecimal("1.00");
            case "SEND_WHATSAPP" -> new BigDecimal("2.50");
            case "SEND_EMAIL", "RETRY" -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }
}
