
package com.myspot.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "guests", indexes = {
    @Index(name = "idx_guest_owner_id", columnList = "pg_owner_id"),
    @Index(name = "idx_guest_email", columnList = "email_address"),
    @Index(name = "idx_guest_phone", columnList = "phone_number"),
    @Index(name = "idx_guest_status", columnList = "guest_status"),
    @Index(name = "idx_guest_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"pgManagementOwner", "bookings"})
@EqualsAndHashCode(of = {"guestId"})
public class Guest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guest_id")
    private Long guestId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pg_owner_id", nullable = false)
    private PGManagementOwner pgManagementOwner;
    
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    @Column(name = "last_name", length = 50)
    private String lastName;
    
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Column(name = "email_address", nullable = false, unique = true, length = 150)
    private String emailAddress;
    
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;
    
    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;
    
    @Column(name = "permanent_address", columnDefinition = "TEXT")
    private String permanentAddress;
    
    @Column(name = "permanent_city", length = 100)
    private String permanentCity;
    
    @Column(name = "permanent_state", length = 100)
    private String permanentState;
    
    @Column(name = "permanent_pincode", length = 10)
    private String permanentPincode;
    
    @Column(name = "occupation", length = 100)
    private String occupation;
    
    @Column(name = "company_name", length = 200)
    private String companyName;
    
    @Column(name = "work_address", columnDefinition = "TEXT")
    private String workAddress;
    
    @Column(name = "monthly_income", length = 50)
    private String monthlyIncome;
    
    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;
    
    @Column(name = "emergency_contact_relation", length = 50)
    private String emergencyContactRelation;
    
    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;
    
    @Column(name = "aadhar_number", length = 20)
    private String aadharNumber;
    
    @Column(name = "pan_number", length = 20)
    private String panNumber;
    
    @Column(name = "aadhar_document_path", length = 500)
    private String aadharDocumentPath;
    
    @Column(name = "pan_document_path", length = 500)
    private String panDocumentPath;
    
    @Column(name = "photo_path", length = 500)
    private String photoPath;
    
    @Column(name = "room_number", length = 10)
    private String roomNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type")
    private Booking.RoomType roomType;
    
    @Builder.Default
    @Column(name = "is_ac_room", nullable = false)
    private Boolean isAcRoom = false;
    
    @Column(name = "bed_number", length = 10)
    private String bedNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "guest_status", nullable = false)
    private GuestStatus guestStatus = GuestStatus.ACTIVE;
    
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "check_in_date")
    private LocalDate checkInDate;
    
    @Column(name = "check_out_date")
    private LocalDate checkOutDate;
    
    @Column(name = "expected_stay_duration", length = 50)
    private String expectedStayDuration;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "food_preference")
    private FoodPreference foodPreference;
    
    @Builder.Default
    @Column(name = "has_food_service", nullable = false)
    private Boolean hasFoodService = false;
    
    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;
    
    @Column(name = "medical_conditions", columnDefinition = "TEXT")
    private String medicalConditions;
    
    @Builder.Default
    @Column(name = "has_vehicle", nullable = false)
    private Boolean hasVehicle = false;
    
    @Column(name = "vehicle_details", length = 200)
    private String vehicleDetails;
    
    @Column(name = "reference_name", length = 100)
    private String referenceName;
    
    @Column(name = "reference_phone", length = 20)
    private String referencePhone;
    
    @Column(name = "reference_relation", length = 50)
    private String referenceRelation;
    
    @Column(name = "security_deposit_paid", precision = 10, scale = 2)
    private java.math.BigDecimal securityDepositPaid;
    
    @Column(name = "monthly_rent", precision = 10, scale = 2)
    private java.math.BigDecimal monthlyRent;
    
    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;
    
    @Column(name = "next_payment_due")
    private LocalDate nextPaymentDue;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    @OneToMany(mappedBy = "guest", fetch = FetchType.LAZY)
    private List<Booking> bookings;
    
    public enum Gender {
        MALE("Male"),
        FEMALE("Female"),
        OTHER("Other");
        
        private final String displayName;
        
        Gender(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum MaritalStatus {
        SINGLE("Single"),
        MARRIED("Married"),
        DIVORCED("Divorced"),
        WIDOWED("Widowed");
        
        private final String displayName;
        
        MaritalStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum GuestStatus {
        ACTIVE("Active"),
        FORMER("Former"),
        SUSPENDED("Suspended"),
        BLACKLISTED("Blacklisted");
        
        private final String displayName;
        
        GuestStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum FoodPreference {
        VEGETARIAN("Vegetarian"),
        NON_VEGETARIAN("Non-Vegetarian"),
        VEGAN("Vegan"),
        JAIN_VEGETARIAN("Jain Vegetarian");
        
        private final String displayName;
        
        FoodPreference(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @PrePersist
    @PreUpdate
    public void setFullName() {
        if (firstName != null) {
            this.fullName = firstName + (lastName != null ? " " + lastName : "");
        }
    }
    
    public boolean isCurrentGuest() {
        return guestStatus == GuestStatus.ACTIVE && 
               checkInDate != null && 
               (checkOutDate == null || checkOutDate.isAfter(LocalDate.now()));
    }
    
    public boolean isFormerGuest() {
        return guestStatus == GuestStatus.FORMER || 
               (checkOutDate != null && checkOutDate.isBefore(LocalDate.now()));
    }
    
    public int getAge() {
        if (dateOfBirth != null) {
            return java.time.Period.between(dateOfBirth, LocalDate.now()).getYears();
        }
        return 0;
    }
}