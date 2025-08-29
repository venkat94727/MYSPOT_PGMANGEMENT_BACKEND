
package com.myspot.backend.controllers;

import com.myspot.backend.services.PGAuthService;
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
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Profile", description = "User profile management APIs")
public class ProfileController {
    
    private final PGAuthService pgAuthService;
    
    @GetMapping
    @Operation(summary = "Get current user profile", description = "Get current user profile information")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentProfile(
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting current profile for PG ID: {}", user.getId());
        
        Map<String, Object> profile = pgAuthService.getCurrentUserProfile(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }
    
    @PutMapping
    @Operation(summary = "Update user profile", description = "Update user profile information")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @RequestBody Map<String, Object> updateData,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Updating profile for PG ID: {}", user.getId());
        
        Map<String, Object> updatedProfile = pgAuthService.updateProfile(user.getId(), updateData);
        
        return ResponseEntity.ok(ApiResponse.success(updatedProfile, "Profile updated successfully"));
    }
}