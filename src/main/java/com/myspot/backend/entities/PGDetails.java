
package com.myspot.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pg_details", indexes = {
    @Index(name = "idx_pg_details_owner_id", columnList = "pg_owner_id"),
    @Index(name = "idx_pg_details_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"pgManagementOwner"})
@EqualsAndHashCode(of = {"pgDetailsId", "pgManagementOwner"})
public class PGDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pg_details_id")
    private Long pgDetailsId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pg_owner_id", nullable = false, unique = true)
    private PGManagementOwner pgManagementOwner;
    
    @Column(name = "pg_description", columnDefinition = "TEXT")
    private String pgDescription;
    
    @Column(name = "pg_type", length = 50)
    private String pgType;
    
    @Column(name = "gender_preference", length = 20)
    private String genderPreference;
    
    @Column(name = "established_year", length = 4)
    private String establishedYear;
    
    @Column(name = "pan_number", length = 20)
    private String panNumber;
    
    @Column(name = "address_line1", length = 200)
    private String addressLine1;
    
    @Column(name = "address_line2", length = 200)
    private String addressLine2;
    
    @Column(name = "locality", length = 100)
    private String locality;
    
    @Column(name = "contact_person_name", length = 100)
    private String contactPersonName;
    
    @Column(name = "contact_mobile_number", length = 20)
    private String contactMobileNumber;
    
    @Column(name = "emergency_contact_number", length = 20)
    private String emergencyContactNumber;
    
    @Column(name = "landline_number", length = 20)
    private String landlineNumber;
    
    @Column(name = "whatsapp_number", length = 20)
    private String whatsappNumber;
    
    @Column(name = "check_in_time", length = 20)
    private String checkInTime;
    
    @Column(name = "check_out_time", length = 20)
    private String checkOutTime;
    
    @Builder.Default
    @Column(name = "single_sharing_available", nullable = false)
    private Boolean singleSharingAvailable = false;
    
    @Builder.Default
    @Column(name = "double_sharing_available", nullable = false)
    private Boolean doubleSharingAvailable = false;
    
    @Builder.Default
    @Column(name = "triple_sharing_available", nullable = false)
    private Boolean tripleSharingAvailable = false;
    
    @Builder.Default
    @Column(name = "quad_sharing_available", nullable = false)
    private Boolean quadSharingAvailable = false;
    
    @Column(name = "single_sharing_cost", precision = 10, scale = 2)
    private BigDecimal singleSharingCost;
    
    @Column(name = "double_sharing_cost_per_person", precision = 10, scale = 2)
    private BigDecimal doubleSharingCostPerPerson;
    
    @Column(name = "triple_sharing_cost_per_person", precision = 10, scale = 2)
    private BigDecimal tripleSharingCostPerPerson;
    
    @Column(name = "quad_sharing_cost_per_person", precision = 10, scale = 2)
    private BigDecimal quadSharingCostPerPerson;
    
    @Builder.Default
    @Column(name = "total_single_sharing_rooms", nullable = false)
    private Integer totalSingleSharingRooms = 0;
    
    @Builder.Default
    @Column(name = "total_double_sharing_rooms", nullable = false)
    private Integer totalDoubleSharingRooms = 0;
    
    @Builder.Default
    @Column(name = "total_triple_sharing_rooms", nullable = false)
    private Integer totalTripleSharingRooms = 0;
    
    @Builder.Default
    @Column(name = "total_quad_sharing_rooms", nullable = false)
    private Integer totalQuadSharingRooms = 0;
    
    @Builder.Default
    @Column(name = "remaining_single_sharing_rooms", nullable = false)
    private Integer remainingSingleSharingRooms = 0;
    
    @Builder.Default
    @Column(name = "remaining_double_sharing_rooms", nullable = false)
    private Integer remainingDoubleSharingRooms = 0;
    
    @Builder.Default
    @Column(name = "remaining_triple_sharing_rooms", nullable = false)
    private Integer remainingTripleSharingRooms = 0;
    
    @Builder.Default
    @Column(name = "remaining_quad_sharing_rooms", nullable = false)
    private Integer remainingQuadSharingRooms = 0;
    
    @Builder.Default
    @Column(name = "ac_rooms_available", nullable = false)
    private Integer acRoomsAvailable = 0;
    
    @Builder.Default
    @Column(name = "non_ac_rooms_available", nullable = false)
    private Integer nonAcRoomsAvailable = 0;
    
    @Builder.Default
    @Column(name = "total_capacity", nullable = false)
    private Integer totalCapacity = 0;
    
    @Builder.Default
    @Column(name = "current_occupancy", nullable = false)
    private Integer currentOccupancy = 0;
    
    @Builder.Default
    @Column(name = "available_beds", nullable = false)
    private Integer availableBeds = 0;
    
    @Builder.Default
    @Column(name = "waiting_list_count", nullable = false)
    private Integer waitingListCount = 0;
    
    @Column(name = "overall_rating", precision = 3, scale = 2)
    private BigDecimal overallRating;
    
    @Builder.Default
    @Column(name = "total_reviews", nullable = false)
    private Integer totalReviews = 0;
    
    @Column(name = "cleanliness_rating", precision = 3, scale = 2)
    private BigDecimal cleanlinessRating;
    
    @Column(name = "location_rating", precision = 3, scale = 2)
    private BigDecimal locationRating;
    
    @Column(name = "value_for_money_rating", precision = 3, scale = 2)
    private BigDecimal valueForMoneyRating;
    
    @Column(name = "staff_behavior_rating", precision = 3, scale = 2)
    private BigDecimal staffBehaviorRating;
    
    @Column(name = "amenities_rating", precision = 3, scale = 2)
    private BigDecimal amenitiesRating;
    
    @Column(name = "food_quality_rating", precision = 3, scale = 2)
    private BigDecimal foodQualityRating;
    
    @Column(name = "virtual_tour_url", length = 500)
    private String virtualTourUrl;
    
    @Column(name = "youtube_video_url", length = 500)
    private String youtubeVideoUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    
    @Column(name = "verification_date")
    private LocalDateTime verificationDate;
    
    @Column(name = "business_registration_doc", length = 500)
    private String businessRegistrationDoc;
    
    @Column(name = "owner_id_proof", length = 500)
    private String ownerIdProof;
    
    @Column(name = "property_documents", length = 500)
    private String propertyDocuments;
    
    @Builder.Default
    @Column(name = "instant_booking_available", nullable = false)
    private Boolean instantBookingAvailable = false;
    
    @Builder.Default
    @Column(name = "partial_payment_allowed", nullable = false)
    private Boolean partialPaymentAllowed = false;
    
    @Column(name = "refund_policy", columnDefinition = "TEXT")
    private String refundPolicy;
    
    @Column(name = "cancellation_policy", columnDefinition = "TEXT")
    private String cancellationPolicy;
    
    @Column(name = "booking_confirmation_time", length = 50)
    private String bookingConfirmationTime;
    
    @Builder.Default
    @Column(name = "student_friendly", nullable = false)
    private Boolean studentFriendly = false;
    
    @Builder.Default
    @Column(name = "professional_friendly", nullable = false)
    private Boolean professionalFriendly = false;
    
    @Builder.Default
    @Column(name = "couples_friendly", nullable = false)
    private Boolean couplesFriendly = false;
    
    @Builder.Default
    @Column(name = "pet_friendly", nullable = false)
    private Boolean petFriendly = false;
    
    @Builder.Default
    @Column(name = "senior_citizen_friendly", nullable = false)
    private Boolean seniorCitizenFriendly = false;
    
    @Builder.Default
    @Column(name = "disabled_friendly", nullable = false)
    private Boolean disabledFriendly = false;
    
    @Builder.Default
    @Column(name = "coworking_space_available", nullable = false)
    private Boolean coworkingSpaceAvailable = false;
    
    @Builder.Default
    @Column(name = "study_room_available", nullable = false)
    private Boolean studyRoomAvailable = false;
    
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @Builder.Default
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
    
    @Column(name = "renewal_date")
    private LocalDateTime renewalDate;
    
    @Builder.Default
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    @OneToMany(mappedBy = "pgDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PGImage> pgImages;
    
    @OneToMany(mappedBy = "pgDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Amenity> amenities;
    
    @OneToMany(mappedBy = "pgDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FoodService> foodServices;
    
    @OneToMany(mappedBy = "pgDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RuleRestriction> rulesRestrictions;
    
    @OneToMany(mappedBy = "pgDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExtraCharge> extraCharges;
    
    public enum VerificationStatus {
        PENDING("Pending"),
        VERIFIED("Verified"),
        REJECTED("Rejected");
        
        private final String displayName;
        
        VerificationStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public Integer getTotalRooms() {
        return (totalSingleSharingRooms != null ? totalSingleSharingRooms : 0) +
               (totalDoubleSharingRooms != null ? totalDoubleSharingRooms : 0) +
               (totalTripleSharingRooms != null ? totalTripleSharingRooms : 0) +
               (totalQuadSharingRooms != null ? totalQuadSharingRooms : 0);
    }
    
    public Integer getRemainingRooms() {
        return (remainingSingleSharingRooms != null ? remainingSingleSharingRooms : 0) +
               (remainingDoubleSharingRooms != null ? remainingDoubleSharingRooms : 0) +
               (remainingTripleSharingRooms != null ? remainingTripleSharingRooms : 0) +
               (remainingQuadSharingRooms != null ? remainingQuadSharingRooms : 0);
    }
    
    public BigDecimal getOccupancyRate() {
        if (totalCapacity == null || totalCapacity == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf((currentOccupancy != null ? currentOccupancy : 0))
                .divide(BigDecimal.valueOf(totalCapacity), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}