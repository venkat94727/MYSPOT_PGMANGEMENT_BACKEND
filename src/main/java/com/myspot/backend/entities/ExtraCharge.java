
package com.myspot.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "extra_charges", indexes = {
    @Index(name = "idx_extra_charge_pg_details", columnList = "pg_details_id"),
    @Index(name = "idx_extra_charge_type", columnList = "charge_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"pgDetails"})
@EqualsAndHashCode(of = {"chargeId"})
public class ExtraCharge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charge_id")
    private Long chargeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pg_details_id", nullable = false)
    private PGDetails pgDetails;
    
    @Column(name = "charge_type", nullable = false, length = 100)
    private String chargeType;
    
    @Column(name = "amount", precision = 8, scale = 2, nullable = false)
    private java.math.BigDecimal amount;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Builder.Default
    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}