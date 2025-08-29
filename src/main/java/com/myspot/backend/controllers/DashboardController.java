
package com.myspot.backend.controllers;

import com.myspot.backend.services.DashboardService;
import com.myspot.backend.dto.response.ApiResponse;
import com.myspot.backend.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Dashboard statistics and overview APIs")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/overview")
    @Operation(summary = "Get dashboard overview", description = "Get dashboard statistics and overview data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardOverview(
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting dashboard overview for PG ID: {}", user.getId());
        
        Map<String, Object> dashboardData = dashboardService.getDashboardOverview(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(dashboardData, "Dashboard overview retrieved successfully"));
    }
    
    @GetMapping("/monthly-stats/{month}")
    @Operation(summary = "Get monthly statistics", description = "Get statistics for a specific month (YYYY-MM format)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyStats(
            @PathVariable String month,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting monthly stats for PG ID: {}, month: {}", user.getId(), month);
        
        Map<String, Object> monthlyStats = dashboardService.getMonthlyStats(user.getId(), month);
        
        return ResponseEntity.ok(ApiResponse.success(monthlyStats, "Monthly statistics retrieved successfully"));
    }
}
