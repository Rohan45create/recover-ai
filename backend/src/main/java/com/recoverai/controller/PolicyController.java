package com.recoverai.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard/policies")
public class PolicyController {

    private List<Policy> mockPolicies = Arrays.asList(
            new Policy("POL-01", "Max Recovery Cost", "Limit the AI's execution budget per case", "5.0", "INR", true),
            new Policy("POL-02", "DND Hours Exclusion", "Block communication during regulatory Do Not Disturb hours", "21:00-08:00", "TIME", true),
            new Policy("POL-03", "Mandate Window Expiry", "Prevent retry actions 24 hours before mandate expires", "24", "HOURS", true),
            new Policy("POL-04", "Escalation Threshold", "Require manual approval for recovery amounts over limit", "50000", "INR", false)
    );

    @GetMapping
    public List<Policy> getPolicies() {
        return mockPolicies;
    }

    @PostMapping
    public List<Policy> updatePolicies(@RequestBody List<Policy> updated) {
        this.mockPolicies = updated;
        return this.mockPolicies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Policy {
        private String id;
        private String name;
        private String description;
        private String value;
        private String unit;
        private boolean active;
    }
}
