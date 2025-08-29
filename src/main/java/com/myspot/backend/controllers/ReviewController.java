
package com.myspot.backend.controllers;

import com.myspot.backend.services.ReviewService;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reviews", description = "Customer review management APIs")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @GetMapping
    @Operation(summary = "Get all reviews", description = "Get all customer reviews for the PG")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllReviews(
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting all reviews for PG ID: {}", user.getId());
        
        List<Map<String, Object>> reviews = reviewService.getAllReviews(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(reviews, "Reviews retrieved successfully"));
    }
    
    @PutMapping("/{reviewId}/response")
    @Operation(summary = "Respond to review", description = "Add or update response to a customer review")
    public ResponseEntity<ApiResponse<String>> respondToReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        String response = request.get("response");
        log.info("Responding to review ID: {} for PG ID: {}", reviewId, user.getId());
        
        reviewService.respondToReview(user.getId(), reviewId, response);
        
        return ResponseEntity.ok(ApiResponse.success("Responded", "Review response added successfully"));
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get review statistics", description = "Get review statistics and ratings summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReviewStats(
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting review statistics for PG ID: {}", user.getId());
        
        Map<String, Object> stats = reviewService.getReviewStats(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(stats, "Review statistics retrieved successfully"));
    }
}
