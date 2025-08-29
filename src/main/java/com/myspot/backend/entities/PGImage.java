
package com.myspot.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "pg_images", indexes = {
    @Index(name = "idx_pg_image_pg_details", columnList = "pg_details_id"),
    @Index(name = "idx_pg_image_type", columnList = "image_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"pgDetails"})
@EqualsAndHashCode(of = {"imageId"})
public class PGImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pg_details_id", nullable = false)
    private PGDetails pgDetails;
    
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
    
    @Column(name = "image_path", nullable = false, length = 500)
    private String imagePath;
    
    @Column(name = "original_filename", length = 255)
    private String originalFilename;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false)
    private ImageType imageType = ImageType.GENERAL;
    
    @Builder.Default
    @Column(name = "is_profile_picture", nullable = false)
    private Boolean isProfilePicture = false;
    
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Builder.Default
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum ImageType {
        PROFILE("Profile Picture"),
        GENERAL("General"),
        ROOM("Room"),
        KITCHEN("Kitchen"), 
        BATHROOM("Bathroom"),
        COMMON_AREA("Common Area"),
        EXTERIOR("Exterior");
        
        private final String displayName;
        ImageType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}