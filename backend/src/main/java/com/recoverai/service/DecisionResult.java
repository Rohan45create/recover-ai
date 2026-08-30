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
    private BigDecimal expectedValue;
}
