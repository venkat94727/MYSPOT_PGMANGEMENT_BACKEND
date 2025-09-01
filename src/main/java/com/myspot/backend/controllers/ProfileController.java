
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
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("pg-auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Profile", description = "User profile management APIs")
public class ProfileController {
    
    private final PGAuthService pgAuthService;
    
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Get current user profile information")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentProfile(
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting current profile for PG ID: {}", user.getId());
        
        Map<String, Object> profile = pgAuthService.getCurrentUserProfile(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }
    
    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update user profile information")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @RequestBody Map<String, Object> updateData,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Updating profile for PG ID: {}", user.getId());
        
        Map<String, Object> updatedProfile = pgAuthService.updateProfile(user.getId(), updateData);
        
        return ResponseEntity.ok(ApiResponse.success(updatedProfile, "Profile updated successfully"));
    }
    
    @PostMapping("/profile/picture")
    @Operation(summary = "Update profile picture", description = "Update user profile picture")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfilePicture(
            @RequestParam("profilePicture") MultipartFile profilePicture,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Updating profile picture for PG ID: {}", user.getId());
        
        try {
            Map<String, Object> result = pgAuthService.updateProfilePicture(user.getId(), profilePicture);
            return ResponseEntity.ok(ApiResponse.success(result, "Profile picture updated successfully"));
        } catch (Exception e) {
            log.error("Failed to update profile picture", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update profile picture: " + e.getMessage()));
        }
    }
    
    @PutMapping("/profile/complete")
    @Operation(summary = "Update complete profile", description = "Update profile with optional picture")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateCompleteProfile(
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestParam("pgName") String pgName,
            @RequestParam("ownerName") String ownerName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("city") String city,
            @RequestParam("state") String state,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "pincode", required = false) String pincode,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Updating complete profile for PG ID: {}", user.getId());
        
        try {
            // Create update data map
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("pgName", pgName);
            updateData.put("ownerName", ownerName);
            updateData.put("phoneNumber", phoneNumber);
            updateData.put("city", city);
            updateData.put("state", state);
            if (country != null) updateData.put("country", country);
            if (pincode != null) updateData.put("pincode", pincode);
            
            // Update profile
            Map<String, Object> updatedProfile = pgAuthService.updateProfile(user.getId(), updateData);
            
            // Update profile picture if provided
            if (profilePicture != null && !profilePicture.isEmpty()) {
                Map<String, Object> pictureResult = pgAuthService.updateProfilePicture(user.getId(), profilePicture);
                // Merge the picture URL into the profile response
                updatedProfile.put("profilePictureUrl", pictureResult.get("profilePictureUrl"));
                updatedProfile.put("profilePictureFileName", pictureResult.get("fileName"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(updatedProfile, "Profile updated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to update complete profile", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to update profile: " + e.getMessage()));
        }
    }

}