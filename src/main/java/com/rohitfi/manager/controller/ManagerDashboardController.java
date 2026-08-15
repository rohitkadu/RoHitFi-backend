package com.rohitfi.manager.controller;

import com.rohitfi.manager.dto.DashboardStatsResponse;
import com.rohitfi.manager.service.ManagerDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/dashboard")
@RequiredArgsConstructor
@Tag(name = "Manager Dashboard", description = "Aggregated banking statistics and operations")
@SecurityRequirement(name = "bearerAuth")
public class ManagerDashboardController {

    private final ManagerDashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Get total bank liquidity, active accounts, and pending requests")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        DashboardStatsResponse stats = dashboardService.getBankStatistics();
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Bank statistics retrieved successfully.");
        response.put("data", stats);
        
        return ResponseEntity.ok(response);
    }
}