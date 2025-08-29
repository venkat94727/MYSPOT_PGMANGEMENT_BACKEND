
package com.myspot.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "amenities", indexes = {
    @Index(name = "idx_amenity_pg_details", columnList = "pg_details_id"),
    @Index(name = "idx_amenity_category", columnList = "category")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"pgDetails"})
@EqualsAndHashCode(of = {"amenityId"})
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "amenity_id")
    private Long amenityId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pg_details_id", nullable = false)
    private PGDetails pgDetails;
    
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Builder.Default
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
    
    @Builder.Default
    @Column(name = "is_free", nullable = false)
    private Boolean isFree = true;
    
    @Column(name = "additional_cost", precision = 8, scale = 2)
    private java.math.BigDecimal additionalCost;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}