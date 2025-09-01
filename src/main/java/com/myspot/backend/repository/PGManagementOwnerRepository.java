
package com.myspot.backend.repository;

import com.myspot.backend.entities.PGManagementOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PGManagementOwnerRepository extends JpaRepository<PGManagementOwner, Long> {
    
    Optional<PGManagementOwner> findByEmailAddress(String emailAddress);
    
    Optional<PGManagementOwner> findByPhoneNumber(String phoneNumber);
    
    Optional<PGManagementOwner> findByEmailAddressAndIsActiveTrue(String emailAddress);
    
    // ADD THESE MISSING METHODS:
    boolean existsByEmailAddress(String emailAddress);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    List<PGManagementOwner> findByVerificationStatus(PGManagementOwner.VerificationStatus status);
    
    List<PGManagementOwner> findByIsActiveTrue();
    
    List<PGManagementOwner> findByCityIgnoreCaseContaining(String city);
    
    List<PGManagementOwner> findByStateIgnoreCaseContaining(String state);
    
    @Query("SELECT p FROM PGManagementOwner p WHERE p.createdAt >= :startDate AND p.createdAt <= :endDate")
    List<PGManagementOwner> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM PGManagementOwner p WHERE p.isActive = true")
    Long countActivePGOwners();
    
    @Query("SELECT COUNT(p) FROM PGManagementOwner p WHERE p.verificationStatus = :status")
    Long countByVerificationStatus(@Param("status") PGManagementOwner.VerificationStatus status);
    
    @Query("SELECT p FROM PGManagementOwner p WHERE " +
           "(:city IS NULL OR LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR LOWER(p.state) LIKE LOWER(CONCAT('%', :state, '%'))) AND " +
           "(:verificationStatus IS NULL OR p.verificationStatus = :verificationStatus)")
    List<PGManagementOwner> findWithFilters(@Param("city") String city,
                                           @Param("state") String state,
                                           @Param("verificationStatus") PGManagementOwner.VerificationStatus verificationStatus);
    
    
    Optional<PGManagementOwner> findByPasswordResetToken(String passwordResetToken);
    
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PGManagementOwner p WHERE p.phoneNumber = :phoneNumber AND p.pgId != :pgId")
    boolean existsByPhoneNumberAndPgIdNot(@Param("phoneNumber") String phoneNumber, @Param("pgId") Long pgId);
    
    // Optional: Add method to find by phone number excluding current user
    @Query("SELECT p FROM PGManagementOwner p WHERE p.phoneNumber = :phoneNumber AND p.pgId != :pgId")
    Optional<PGManagementOwner> findByPhoneNumberAndPgIdNot(@Param("phoneNumber") String phoneNumber, @Param("pgId") Long pgId);
}
