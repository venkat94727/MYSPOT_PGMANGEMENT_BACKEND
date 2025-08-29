
package com.myspot.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_review_owner_id", columnList = "pg_owner_id"),
    @Index(name = "idx_review_guest_id", columnList = "guest_id"),
    @Index(name = "idx_review_rating", columnList = "overall_rating")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"pgManagementOwner", "guest"})
@EqualsAndHashCode(of = {"reviewId"})
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pg_owner_id", nullable = false)
    private PGManagementOwner pgManagementOwner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;
    
    @Column(name = "guest_name", nullable = false, length = 100)
    private String guestName;
    
    @Column(name = "overall_rating", precision = 3, scale = 2, nullable = false)
    private java.math.BigDecimal overallRating;
    
    @Column(name = "cleanliness_rating", precision = 3, scale = 2)
    private java.math.BigDecimal cleanlinessRating;
    
    @Column(name = "location_rating", precision = 3, scale = 2)
    private java.math.BigDecimal locationRating;
    
    @Column(name = "value_for_money_rating", precision = 3, scale = 2)
    private java.math.BigDecimal valueForMoneyRating;
    
    @Column(name = "staff_behavior_rating", precision = 3, scale = 2)
    private java.math.BigDecimal staffBehaviorRating;
    
    @Column(name = "amenities_rating", precision = 3, scale = 2)
    private java.math.BigDecimal amenitiesRating;
    
    @Column(name = "food_quality_rating", precision = 3, scale = 2)
    private java.math.BigDecimal foodQualityRating;
    
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
    
    @Column(name = "response", columnDefinition = "TEXT")
    private String response;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
