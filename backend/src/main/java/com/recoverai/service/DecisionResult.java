package com.recoverai.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionResult {
    private String chosenAction;
    private BigDecimal expectedRecoveryValue;
    
    // Computation details for audit logging
    private BigDecimal probabilityOfRecovery;
    private BigDecimal baseAmount;
    private BigDecimal interventionCost;
    private BigDecimal frictionPenalty;
    
    public DecisionResult(String chosenAction, BigDecimal expectedRecoveryValue) {
        this.chosenAction = chosenAction;
        this.expectedRecoveryValue = expectedRecoveryValue;
    }
}
