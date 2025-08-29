
package com.myspot.backend.controllers;

import com.myspot.backend.services.PGService;
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

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pg")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "PG Management", description = "PG profile and details management APIs")
public class PGController {
    
    private final PGService pgService;
    
    @GetMapping("/details")
    @Operation(summary = "Get PG details", description = "Get complete PG information and settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPGDetails(
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting PG details for PG ID: {}", user.getId());
        
        Map<String, Object> pgDetails = pgService.getPGDetails(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(pgDetails, "PG details retrieved successfully"));
    }
    
    @PutMapping("/details")
    @Operation(summary = "Update PG details", description = "Update PG information and settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePGDetails(
            @RequestBody Map<String, Object> updateData,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Updating PG details for PG ID: {}", user.getId());
        
        Map<String, Object> updatedDetails = pgService.updatePGDetails(user.getId(), updateData);
        
        return ResponseEntity.ok(ApiResponse.success(updatedDetails, "PG details updated successfully"));
    }
    
    @PostMapping("/profile-picture")
    @Operation(summary = "Upload profile picture", description = "Upload PG profile picture")
    public ResponseEntity<ApiResponse<String>> uploadProfilePicture(
            @RequestParam("profilePicture") MultipartFile file,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Uploading profile picture for PG ID: {}", user.getId());
        
        String imageUrl = pgService.uploadProfilePicture(user.getId(), file);
        
        return ResponseEntity.ok(ApiResponse.success(imageUrl, "Profile picture uploaded successfully"));
    }
    
    @PostMapping("/pictures")
    @Operation(summary = "Upload PG pictures", description = "Upload multiple PG pictures")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> uploadPGPictures(
            @RequestParam("pgPictures") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Uploading {} PG pictures for PG ID: {}", files.size(), user.getId());
        
        List<Map<String, Object>> uploadedPictures = pgService.uploadPGPictures(user.getId(), files);
        
        return ResponseEntity.ok(ApiResponse.success(uploadedPictures, "PG pictures uploaded successfully"));
    }
    
    @PutMapping("/pictures/{imageId}")
    @Operation(summary = "Update picture description", description = "Update description of a PG picture")
    public ResponseEntity<ApiResponse<String>> updatePictureDescription(
            @PathVariable Long imageId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Updating picture description for image ID: {} in PG ID: {}", imageId, user.getId());
        
        String description = request.get("description");
        pgService.updatePictureDescription(user.getId(), imageId, description);
        
        return ResponseEntity.ok(ApiResponse.success("Updated", "Picture description updated successfully"));
    }
    
    @DeleteMapping("/pictures/{imageId}")
    @Operation(summary = "Delete PG picture", description = "Delete a PG picture")
    public ResponseEntity<ApiResponse<String>> deletePGPicture(
            @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Deleting PG picture ID: {} for PG ID: {}", imageId, user.getId());
        
        pgService.deletePGPicture(user.getId(), imageId);
        
        return ResponseEntity.ok(ApiResponse.success("Deleted", "PG picture deleted successfully"));
    }
}
