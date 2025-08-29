package com.myspot.backend.repository;

import com.myspot.backend.entities.PGImage;
import com.myspot.backend.entities.PGDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PGImageRepository extends JpaRepository<PGImage, Long> {
    
    @Query("SELECT pi FROM PGImage pi WHERE pi.pgDetails.pgDetailsId = :pgDetailsId AND pi.isActive = true ORDER BY pi.displayOrder ASC")
    List<PGImage> findActiveImagesByPgDetailsId(@Param("pgDetailsId") Long pgDetailsId);
    
    @Query("SELECT pi FROM PGImage pi WHERE pi.pgDetails.pgDetailsId = :pgDetailsId AND pi.isProfilePicture = true AND pi.isActive = true")
    Optional<PGImage> findProfilePictureByPgDetailsId(@Param("pgDetailsId") Long pgDetailsId);
    
    List<PGImage> findByPgDetails(PGDetails pgDetails);
}
