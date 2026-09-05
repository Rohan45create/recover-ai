package com.recoverai.controller;

import com.recoverai.domain.policy.Policy;
import com.recoverai.service.PolicyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    /** GET /api/dashboard/policies — returns all policies from DB */
    @GetMapping
    public List<Policy> getPolicies() {
        return policyService.getAllPolicies();
    }

    /** PUT /api/dashboard/policies/{id} — update a single policy (value and/or enabled) */
    @PutMapping("/{id}")
    public ResponseEntity<Policy> updatePolicy(
            @PathVariable String id,
            @RequestBody PolicyUpdateRequest body) {
        try {
            Policy updated = policyService.updatePolicy(id, body.getValue(), body.getEnabled(), "dashboard-user");
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** POST /api/dashboard/policies — legacy bulk-update kept for backward compat; updates each policy individually */
    @PostMapping
    public List<Policy> updatePolicies(@RequestBody List<PolicyUpdateRequest> updates) {
        updates.forEach(u -> {
            if (u.getId() != null) {
                policyService.updatePolicy(u.getId(), u.getValue(), u.getEnabled(), "dashboard-user");
            }
        });
        return policyService.getAllPolicies();
    }

    @Data
    public static class PolicyUpdateRequest {
        private String id;
        private String value;
        private Boolean enabled;
    }
}
