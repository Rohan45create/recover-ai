package com.recoverai.controller;

import com.recoverai.domain.recovery.CaseState;
import com.recoverai.domain.recovery.RecoveryCase;
import com.recoverai.dto.DashboardOverviewResponse;
import com.recoverai.repository.RecoveryCaseRepository;
import com.recoverai.service.DashboardService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")

@RequiredArgsConstructor
public class DashboardController {

    private final RecoveryCaseRepository recoveryCaseRepository;
    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public DashboardOverviewResponse getOverview() {
        return dashboardService.getOverviewMetrics();
    }

    // Keep this for backward compatibility if needed temporarily
    @GetMapping("/kpis")
    public DashboardKpis getKpis() {
        List<RecoveryCase> allCases = recoveryCaseRepository.findAll();
        
        long totalCases = allCases.size();
        long recoveredCases = allCases.stream().filter(rc -> rc.getStatus() == CaseState.RECOVERED).count();
        long activeCases = allCases.stream().filter(rc -> 
            rc.getStatus() != CaseState.RECOVERED && 
            rc.getStatus() != CaseState.STOPPED).count();
            
        BigDecimal totalRecovered = allCases.stream()
            .filter(rc -> rc.getStatus() == CaseState.RECOVERED && rc.getExpectedValue() != null)
            .map(RecoveryCase::getExpectedValue) // Approximate based on what we expected
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        double recoveryRate = totalCases > 0 ? ((double) recoveredCases / totalCases) * 100.0 : 0.0;
        
        return DashboardKpis.builder()
            .totalRecoveredAmount(totalRecovered.setScale(2, RoundingMode.HALF_UP))
            .recoveryRatePercentage(Math.round(recoveryRate * 10.0) / 10.0)
            .activeCases(activeCases)
            .totalCases(totalCases)
            .policyViolations(0) // Safe default
            .build();
    }

    @GetMapping("/cases")
    public Page<RecoveryCase> getRecentCases(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return recoveryCaseRepository.findAll(pageable);
    }
    
    @GetMapping("/cases/{id}/timeline")
    public List<com.recoverai.domain.audit.AuditEvent> getCaseTimeline(@PathVariable java.util.UUID id) {
        return dashboardService.getCaseTimeline(id);
    }
    
    @GetMapping("/analytics")
    public com.recoverai.dto.DashboardAnalyticsResponse getAnalytics() {
        return dashboardService.getAnalytics();
    }
    
    @Data
    @Builder
    public static class DashboardKpis {
        private BigDecimal totalRecoveredAmount;
        private double recoveryRatePercentage;
        private long activeCases;
        private long totalCases;
        private int policyViolations;
    }
}
