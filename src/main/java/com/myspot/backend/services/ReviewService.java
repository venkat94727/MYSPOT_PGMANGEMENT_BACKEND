
package com.myspot.backend.services;

import com.myspot.backend.entities.*;
import com.myspot.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    
    private final PGManagementOwnerRepository pgManagementOwnerRepository;
    private final ReviewRepository reviewRepository;
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllReviews(Long pgId) {
        log.info("Getting all reviews for PG ID: {}", pgId);
        
        List<Review> reviews = reviewRepository.findActiveReviewsByPgId(pgId);
        
        return reviews.stream()
            .map(this::convertReviewToMap)
            .collect(Collectors.toList());
    }
    
    public void respondToReview(Long pgId, Long reviewId, String response) {
        log.info("Responding to review ID: {} for PG ID: {}", reviewId, pgId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        if (!review.getPgManagementOwner().getPgId().equals(pgId)) {
            throw new RuntimeException("Review does not belong to this PG");
        }
        
        review.setResponse(response);
        review.setRespondedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getReviewStats(Long pgId) {
        log.info("Getting review statistics for PG ID: {}", pgId);
        
        Map<String, Object> stats = new HashMap<>();
        
        Long totalReviews = reviewRepository.countActiveReviewsByPgId(pgId);
        BigDecimal averageRating = reviewRepository.getAverageRatingByPgId(pgId);
        
        stats.put("totalReviews", totalReviews != null ? totalReviews.intValue() : 0);
        stats.put("averageRating", averageRating != null ? averageRating : BigDecimal.ZERO);
        
        // Individual rating averages
        BigDecimal cleanlinessRating = reviewRepository.getAverageCleanlinessRating(pgId);
        BigDecimal locationRating = reviewRepository.getAverageLocationRating(pgId);
        BigDecimal valueForMoneyRating = reviewRepository.getAverageValueForMoneyRating(pgId);
        BigDecimal staffBehaviorRating = reviewRepository.getAverageStaffBehaviorRating(pgId);
        BigDecimal amenitiesRating = reviewRepository.getAverageAmenitiesRating(pgId);
        BigDecimal foodQualityRating = reviewRepository.getAverageFoodQualityRating(pgId);
        
        Map<String, BigDecimal> categoryRatings = new HashMap<>();
        categoryRatings.put("cleanliness", cleanlinessRating != null ? cleanlinessRating : BigDecimal.ZERO);
        categoryRatings.put("location", locationRating != null ? locationRating : BigDecimal.ZERO);
        categoryRatings.put("valueForMoney", valueForMoneyRating != null ? valueForMoneyRating : BigDecimal.ZERO);
        categoryRatings.put("staffBehavior", staffBehaviorRating != null ? staffBehaviorRating : BigDecimal.ZERO);
        categoryRatings.put("amenities", amenitiesRating != null ? amenitiesRating : BigDecimal.ZERO);
        categoryRatings.put("foodQuality", foodQualityRating != null ? foodQualityRating : BigDecimal.ZERO);
        
        stats.put("categoryRatings", categoryRatings);
        
        // Rating distribution (you can implement this based on your needs)
        Map<String, Integer> ratingDistribution = new HashMap<>();
        ratingDistribution.put("5star", 0);
        ratingDistribution.put("4star", 0);
        ratingDistribution.put("3star", 0);
        ratingDistribution.put("2star", 0);
        ratingDistribution.put("1star", 0);
        stats.put("ratingDistribution", ratingDistribution);
        
        return stats;
    }
    
    private Map<String, Object> convertReviewToMap(Review review) {
        Map<String, Object> reviewMap = new HashMap<>();
        
        reviewMap.put("reviewId", review.getReviewId());
        reviewMap.put("guestName", review.getGuestName());
        reviewMap.put("overallRating", review.getOverallRating());
        reviewMap.put("cleanlinessRating", review.getCleanlinessRating());
        reviewMap.put("locationRating", review.getLocationRating());
        reviewMap.put("valueForMoneyRating", review.getValueForMoneyRating());
        reviewMap.put("staffBehaviorRating", review.getStaffBehaviorRating());
        reviewMap.put("amenitiesRating", review.getAmenitiesRating());
        reviewMap.put("foodQualityRating", review.getFoodQualityRating());
        reviewMap.put("comment", review.getComment());
        reviewMap.put("response", review.getResponse());
        reviewMap.put("respondedAt", review.getRespondedAt());
        reviewMap.put("isActive", review.getIsActive());
        reviewMap.put("isVerified", review.getIsVerified());
        reviewMap.put("createdAt", review.getCreatedAt());
        reviewMap.put("updatedAt", review.getUpdatedAt());
        
        // Guest information
        if (review.getGuest() != null) {
            Map<String, Object> guestInfo = new HashMap<>();
            guestInfo.put("guestId", review.getGuest().getGuestId());
            guestInfo.put("fullName", review.getGuest().getFullName());
            reviewMap.put("guest", guestInfo);
        }
        
        return reviewMap;
    }
}
