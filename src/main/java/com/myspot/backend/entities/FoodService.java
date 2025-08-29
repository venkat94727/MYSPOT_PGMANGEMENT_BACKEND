
package com.myspot.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_services", indexes = {
    @Index(name = "idx_food_service_pg_details", columnList = "pg_details_id"),
    @Index(name = "idx_food_service_type", columnList = "service_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"pgDetails"})
@EqualsAndHashCode(of = {"foodServiceId"})
public class FoodService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_service_id")
    private Long foodServiceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pg_details_id", nullable = false)
    private PGDetails pgDetails;
    
    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType;
    
    @Column(name = "timing", nullable = false, length = 100)
    private String timing;
    
    @Column(name = "cost", precision = 8, scale = 2, nullable = false)
    private java.math.BigDecimal cost;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Builder.Default
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
    
    @Builder.Default
    @Column(name = "is_vegetarian", nullable = false)
    private Boolean isVegetarian = true;
    
    @Builder.Default
    @Column(name = "is_non_vegetarian", nullable = false)
    private Boolean isNonVegetarian = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}