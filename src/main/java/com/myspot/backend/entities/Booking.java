package com.myspot.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_owner_id", columnList = "pg_owner_id"),
    @Index(name = "idx_booking_guest_id", columnList = "guest_id"),
    @Index(name = "idx_booking_status", columnList = "status"),
    @Index(name = "idx_booking_check_in", columnList = "check_in_date"),
    @Index(name = "idx_booking_check_out", columnList = "check_out_date"),
    @Index(name = "idx_booking_date", columnList = "booking_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"pgManagementOwner", "guest"})
@EqualsAndHashCode(of = {"bookingId"})
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;
    
    @Column(name = "booking_reference", unique = true, nullable = false, length = 20)
    private String bookingReference;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pg_owner_id", nullable = false)
    private PGManagementOwner pgManagementOwner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;
    
    @Column(name = "guest_name", nullable = false, length = 100)
    private String guestName;
    
    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;
    
    @Column(name = "email_address", length = 150)
    private String emailAddress;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;
    
    @Column(name = "room_number", length = 10)
    private String roomNumber;
    
    @Builder.Default
    @Column(name = "is_ac_room", nullable = false)
    private Boolean isAcRoom = false;
    
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;
    
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;
    
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;
    
    @Column(name = "actual_check_in")
    private LocalDateTime actualCheckIn;
    
    @Column(name = "actual_check_out")
    private LocalDateTime actualCheckOut;
    
    @Column(name = "base_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal baseAmount;
    
    @Column(name = "extra_charges", precision = 10, scale = 2)
    private BigDecimal extraCharges = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    @Column(name = "advance_paid", precision = 10, scale = 2)
    private BigDecimal advancePaid = BigDecimal.ZERO;
    
    @Column(name = "remaining_amount", precision = 10, scale = 2)
    private BigDecimal remainingAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancelled_by", length = 100)
    private String cancelledBy;
    
    @Column(name = "duration_days")
    private Integer durationDays;
    
    @Column(name = "duration_months")
    private Integer durationMonths;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    public enum RoomType {
        SINGLE_SHARING("Single Sharing"),
        DOUBLE_SHARING("Double Sharing"),
        TRIPLE_SHARING("Triple Sharing"),
        QUAD_SHARING("Quad Sharing");
        
        private final String displayName;
        
        RoomType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum BookingStatus {
        PENDING("Pending"),
        CONFIRMED("Confirmed"),
        ACTIVE("Active"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        NO_SHOW("No Show");
        
        private final String displayName;
        
        BookingStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum PaymentStatus {
        PENDING("Pending"),
        ADVANCE_PAID("Advance Paid"),
        PAID("Paid"),
        REFUNDED("Refunded"),
        PARTIALLY_REFUNDED("Partially Refunded");
        
        private final String displayName;
        
        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @PrePersist
    public void generateBookingReference() {
        if (bookingReference == null) {
            this.bookingReference = "BK" + String.format("%06d", System.currentTimeMillis() % 1000000);
        }
    }
    
    public void calculateTotalAmount() {
        this.totalAmount = baseAmount
            .add(extraCharges != null ? extraCharges : BigDecimal.ZERO)
            .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        
        this.remainingAmount = totalAmount
            .subtract(advancePaid != null ? advancePaid : BigDecimal.ZERO);
    }
    
    public void calculateDuration() {
        if (checkInDate != null && checkOutDate != null) {
            this.durationDays = (int) java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            this.durationMonths = durationDays / 30;
        }
    }
    
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return status == BookingStatus.ACTIVE || 
               (status == BookingStatus.CONFIRMED && 
                !checkInDate.isAfter(today) && 
                !checkOutDate.isBefore(today));
    }
    
    public boolean isUpcoming() {
        return status == BookingStatus.CONFIRMED && checkInDate.isAfter(LocalDate.now());
    }
    
    public boolean isOverdue() {
        return status == BookingStatus.ACTIVE && checkOutDate.isBefore(LocalDate.now());
    }
}