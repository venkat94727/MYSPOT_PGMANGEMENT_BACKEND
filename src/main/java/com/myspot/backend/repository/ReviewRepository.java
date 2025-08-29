
package com.myspot.backend.repository;

import com.myspot.backend.entities.Review;
import com.myspot.backend.entities.PGManagementOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPgManagementOwner(PGManagementOwner pgManagementOwner);
    List<Review> findByPgManagementOwner_PgId(Long pgId);
    List<Review> findByPgManagementOwnerOrderByCreatedAtDesc(PGManagementOwner pgManagementOwner);
    
    @Query("SELECT r FROM Review r WHERE r.pgManagementOwner.pgId = :pgId AND r.isActive = true ORDER BY r.createdAt DESC")
    List<Review> findActiveReviewsByPgId(@Param("pgId") Long pgId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.pgManagementOwner.pgId = :pgId AND r.isActive = true")
    Long countActiveReviewsByPgId(@Param("pgId") Long pgId);
    
    @Query("SELECT AVG(r.overallRating) FROM Review r WHERE r.pgManagementOwner.pgId = :pgId AND r.isActive = true")
    java.math.BigDecimal getAverageRatingByPgId(@Param("pgId") Long pgId);
    
    @Query("SELECT AVG(r.cleanlinessRating) FROM Review r WHERE r.pgManagementOwner.pgId = :pgId AND r.isActive = true AND r.cleanlinessRating IS NOT NULL")
    java.math.BigDecimal getAverageCleanlinessRating(@Param("pgId") Long pgId);
    
    @Query("SELECT AVG(r.locationRating) FROM Review r WHERE r.pgManagementOwner.pgId = :pgId AND r.isActive = true AND r.locationRating IS NOT NULL")
    java.math.BigDecimal getAverageLocationRating(@Param("pgId") Long pgId);
    
    @Query("SELECT AVG(r.valueForMoneyRating) FROM Review r WHERE r.pgManagementOwner.pgId = :pgId AND r.isActive = true AND r.valueForMoneyRating IS NOT NULL")
    java.math.BigDecimal getAverageValueForMoneyRating(@Param("pgId") Long pgId);
    
    @Query("SELECT AVG(r.staffBehaviorRating) FROM Review r WHERE r.pgManagementOwner.pgId = :pgId AND r.isActive = true AND r.staffBehaviorRating IS NOT NULL")
    java.math.BigDecimal getAverageStaffBehaviorRating(@Param("pgId") Long pgId);
    
    @Query("SELECT AVG(r.amenitiesRating) FROM Review r WHERE r.pgManagementOwner.pgId = :pgId AND r.isActive = true AND r.amenitiesRating IS NOT NULL")
    java.math.BigDecimal getAverageAmenitiesRating(@Param("pgId") Long pgId);
    
    @Query("SELECT AVG(r.foodQualityRating) FROM Review r WHERE r.pgManagementOwner.pgId = :pgId AND r.isActive = true AND r.foodQualityRating IS NOT NULL")
    java.math.BigDecimal getAverageFoodQualityRating(@Param("pgId") Long pgId);
}
