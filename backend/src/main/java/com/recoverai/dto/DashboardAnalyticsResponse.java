package com.recoverai.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalyticsResponse {

    private List<DiagnosisSplit> diagnosisSplit;
    private List<ActionEffectiveness> actionEffectiveness;
    private Double decisionAccuracy;
    private double policyBlockRate;

    @Data
    @AllArgsConstructor
    public static class DiagnosisSplit {
        private String diagnosis;
        private int count;
        private double percentage;
    }

    @Data
    @AllArgsConstructor
    public static class ActionEffectiveness {
        private String action;
        private int attemptCount;
        private int successCount;
        private BigDecimal totalRecovered;
        private double successRate;
    }
}
