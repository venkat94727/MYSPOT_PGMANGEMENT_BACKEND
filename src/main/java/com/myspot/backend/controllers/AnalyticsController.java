
package com.myspot.backend.controllers;

import com.myspot.backend.services.AnalyticsService;
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
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Analytics", description = "Analytics and reporting APIs")
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    @GetMapping("/revenue")
    @Operation(summary = "Get revenue analytics", description = "Get revenue analytics and trends")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRevenueAnalytics(
            @RequestParam(required = false, defaultValue = "all") String month,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting revenue analytics for PG ID: {}, month: {}", user.getId(), month);
        
        Map<String, Object> analytics = analyticsService.getRevenueAnalytics(user.getId(), month);
        
        return ResponseEntity.ok(ApiResponse.success(analytics, "Revenue analytics retrieved successfully"));
    }
    
    @GetMapping("/occupancy")
    @Operation(summary = "Get occupancy analytics", description = "Get occupancy rates and trends")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOccupancyAnalytics(
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting occupancy analytics for PG ID: {}", user.getId());
        
        Map<String, Object> analytics = analyticsService.getOccupancyAnalytics(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(analytics, "Occupancy analytics retrieved successfully"));
    }
    
    @GetMapping("/monthly-report/{month}")
    @Operation(summary = "Get monthly report", description = "Get comprehensive monthly report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyReport(
            @PathVariable String month,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting monthly report for PG ID: {}, month: {}", user.getId(), month);
        
        Map<String, Object> report = analyticsService.getMonthlyReport(user.getId(), month);
        
        return ResponseEntity.ok(ApiResponse.success(report, "Monthly report retrieved successfully"));
    }
}
