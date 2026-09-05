package com.recoverai.service;

import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.RecoveryCase;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DecisionService {
    
    private static final Logger log = LoggerFactory.getLogger(DecisionService.class);

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

        BigDecimal pRecovery = getRecoveryProbability(rc.getDiagnosis(), bestAction);
        BigDecimal cost = getActionCost(bestAction);
        BigDecimal amount = payment.getAmount();
        String frictionPenalty = "0.00"; // Dummy friction penalty requested by user
        
        log.info("[DECISION] case={} action={} P(recovery)={} amount={} interventionCost={} frictionPenalty={} => expectedValue=({}*{})-{}-{} = {}", 
                 rc.getId(), bestAction, pRecovery, amount, cost, frictionPenalty, 
                 pRecovery, amount, cost, frictionPenalty, bestEV);

        return new DecisionResult(
            bestAction, 
            bestEV, 
            pRecovery, 
            amount, 
            cost, 
            new BigDecimal(frictionPenalty)
        );
    }

    private BigDecimal getRecoveryProbability(String diagnosis, String action) {
        // Hand-specified lookup table per architecture specs.
        // In real life this would be a DB table or a small model.
        if (diagnosis == null) return new BigDecimal("0.05");

        return switch (diagnosis) {
            case "INSUFFICIENT_FUNDS" -> switch (action) {
                case "RETRY" -> new BigDecimal("0.40");
                case "CREATE_PAYMENT_LINK" -> new BigDecimal("0.65"); // Dedicated link removes friction, higher than retry
                case "SEND_SMS" -> new BigDecimal("0.15");
                case "SEND_WHATSAPP" -> new BigDecimal("0.20");
                default -> new BigDecimal("0.05");
            };
            case "TEMPORARY_SYSTEM_FAILURE" -> switch (action) {
                case "RETRY" -> new BigDecimal("0.80");
                case "CREATE_PAYMENT_LINK" -> new BigDecimal("0.50"); // Less relevant than retry for system failures
                default -> new BigDecimal("0.10");
            };
            case "FRAUD_SUSPECTED" -> new BigDecimal("0.00"); // Never try to recover fraud
            default -> switch (action) {
                case "RETRY" -> new BigDecimal("0.10");
                case "CREATE_PAYMENT_LINK" -> new BigDecimal("0.35"); // Link still more effective than email for unknown causes
                case "SEND_EMAIL" -> new BigDecimal("0.10");
                default -> new BigDecimal("0.05");
            };
        };
    }

    private BigDecimal getActionCost(String action) {
        return switch (action) {
            case "SEND_SMS" -> new BigDecimal("1.00");
            case "SEND_WHATSAPP" -> new BigDecimal("2.50");
            case "SEND_EMAIL", "RETRY", "CREATE_PAYMENT_LINK" -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }
}
