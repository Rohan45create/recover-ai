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
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final RecoveryCaseRepository recoveryCaseRepository;
    private final DashboardService dashboardService;
    private final ObjectMapper objectMapper;

    @GetMapping("/overview")
    public DashboardOverviewResponse getOverview(
            @RequestParam(value = "range", defaultValue = "30d") String range) {
        return dashboardService.getOverviewMetrics(range);
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
            .filter(rc -> rc.getStatus() == CaseState.RECOVERED && rc.getExpectedRecoveryValue() != null)
            .map(RecoveryCase::getExpectedRecoveryValue)
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
    public List<Map<String, Object>> getCaseTimeline(@PathVariable java.util.UUID id) {
        return dashboardService.getCaseTimeline(id).stream().map(event -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", event.getId());
            m.put("case_id", event.getCaseId());
            m.put("event_type", event.getEventType());
            m.put("sequence_no", event.getSequenceNo());
            m.put("created_at", event.getCreatedAt());
            // Parse the JSON payload string into a details object so the frontend can read fields directly
            try {
                Object details = objectMapper.readValue(event.getPayload(), Object.class);
                m.put("details", details);
            } catch (Exception e) {
                m.put("details", Map.of("raw", event.getPayload()));
            }
            return m;
        }).collect(Collectors.toList());
    }
    
    @GetMapping("/analytics")
    public com.recoverai.dto.DashboardAnalyticsResponse getAnalytics(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String diagnosis,
            @RequestParam(required = false) String action) {
        return dashboardService.getAnalytics(range, diagnosis, action);
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
