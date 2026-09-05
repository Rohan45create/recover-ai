package com.recoverai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {
    private BigDecimal totalRecoveredAmount;
    private double recoveryRatePercentage;
    private long activeCases;
    private long totalCases;
    private long policyViolations;
    private double efficiencyScore;
    private List<TrajectoryPoint> trajectory;
}
