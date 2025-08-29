

package com.myspot.backend.controllers;

import com.myspot.backend.services.GuestService;
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

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/guests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Guest Management", description = "Guest and tenant management APIs")
public class GuestController {
    
    private final GuestService guestService;
    
    @GetMapping
    @Operation(summary = "Get all guests", description = "Get all guests with optional status filter")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllGuests(
            @RequestParam(required = false, defaultValue = "all") String status,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting all guests for PG ID: {}, status: {}", user.getId(), status);
        
        List<Map<String, Object>> guests = guestService.getAllGuests(user.getId(), status);
        
        return ResponseEntity.ok(ApiResponse.success(guests, "Guests retrieved successfully"));
    }
    
    @GetMapping("/{guestId}")
    @Operation(summary = "Get guest details", description = "Get detailed information for a specific guest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGuestDetails(
            @PathVariable Long guestId,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting guest details for guest ID: {} in PG ID: {}", guestId, user.getId());
        
        Map<String, Object> guest = guestService.getGuestDetails(user.getId(), guestId);
        
        return ResponseEntity.ok(ApiResponse.success(guest, "Guest details retrieved successfully"));
    }
    
    @PutMapping("/{guestId}/status")
    @Operation(summary = "Update guest status", description = "Update the status of a guest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateGuestStatus(
            @PathVariable Long guestId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        String newStatus = request.get("status");
        log.info("Updating guest status for guest ID: {} to status: {}", guestId, newStatus);
        
        Map<String, Object> updatedGuest = guestService.updateGuestStatus(user.getId(), guestId, newStatus);
        
        return ResponseEntity.ok(ApiResponse.success(updatedGuest, "Guest status updated successfully"));
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get guest statistics", description = "Get guest statistics for dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGuestStats(
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting guest statistics for PG ID: {}", user.getId());
        
        Map<String, Object> stats = guestService.getGuestStats(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(stats, "Guest statistics retrieved successfully"));
    }
}
