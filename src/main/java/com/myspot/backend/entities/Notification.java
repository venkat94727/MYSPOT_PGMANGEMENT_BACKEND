
package com.myspot.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_owner_id", columnList = "pg_owner_id"),
    @Index(name = "idx_notification_type", columnList = "notification_type"),
    @Index(name = "idx_notification_read", columnList = "is_read")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"pgManagementOwner"})
@EqualsAndHashCode(of = {"notificationId"})
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pg_owner_id", nullable = false)
    private PGManagementOwner pgManagementOwner;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority = Priority.MEDIUM;
    
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "action_url", length = 500)
    private String actionUrl;
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
    
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum NotificationType {
        BOOKING_CONFIRMED("Booking Confirmed"),
        BOOKING_CANCELLED("Booking Cancelled"),
        PAYMENT_RECEIVED("Payment Received"),
        PAYMENT_OVERDUE("Payment Overdue"),
        NEW_REVIEW("New Review"),
        GUEST_CHECK_IN("Guest Check-in"),
        GUEST_CHECK_OUT("Guest Check-out"),
        SYSTEM_ALERT("System Alert"),
        MAINTENANCE_REMINDER("Maintenance Reminder");
        
        private final String displayName;
        NotificationType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum Priority {
        LOW("Low"), MEDIUM("Medium"), HIGH("High"), URGENT("Urgent");
        
        private final String displayName;
        Priority(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}
