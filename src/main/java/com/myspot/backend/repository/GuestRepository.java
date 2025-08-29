
package com.myspot.backend.repository;

import com.myspot.backend.entities.Guest;
import com.myspot.backend.entities.PGManagementOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByPgManagementOwner(PGManagementOwner pgManagementOwner);
    List<Guest> findByPgManagementOwner_PgId(Long pgId);
    List<Guest> findByPgManagementOwnerOrderByCreatedAtDesc(PGManagementOwner pgManagementOwner);
    
    Optional<Guest> findByEmailAddress(String emailAddress);
    Optional<Guest> findByPhoneNumber(String phoneNumber);
    
    List<Guest> findByPgManagementOwnerAndGuestStatus(PGManagementOwner pgManagementOwner, Guest.GuestStatus status);
    
    @Query("SELECT g FROM Guest g WHERE g.pgManagementOwner.pgId = :pgId AND g.guestStatus = :status ORDER BY g.createdAt DESC")
    List<Guest> findByPgIdAndStatus(@Param("pgId") Long pgId, @Param("status") Guest.GuestStatus status);
    
    @Query("SELECT g FROM Guest g WHERE g.pgManagementOwner.pgId = :pgId " +
           "AND g.guestStatus = 'ACTIVE' AND g.isActive = true")
    List<Guest> findActiveGuests(@Param("pgId") Long pgId);
    
    @Query("SELECT g FROM Guest g WHERE g.pgManagementOwner.pgId = :pgId " +
           "AND (g.guestStatus = 'FORMER' OR g.checkOutDate < CURRENT_DATE)")
    List<Guest> findFormerGuests(@Param("pgId") Long pgId);
    
    @Query("SELECT COUNT(g) FROM Guest g WHERE g.pgManagementOwner.pgId = :pgId")
    Long countByPgId(@Param("pgId") Long pgId);
    
    @Query("SELECT COUNT(g) FROM Guest g WHERE g.pgManagementOwner.pgId = :pgId AND g.guestStatus = :status")
    Long countByPgIdAndStatus(@Param("pgId") Long pgId, @Param("status") Guest.GuestStatus status);
    
    @Query("SELECT COUNT(g) FROM Guest g WHERE g.pgManagementOwner.pgId = :pgId AND g.guestStatus = 'ACTIVE'")
    Long countActiveGuests(@Param("pgId") Long pgId);
    
    @Query("SELECT COUNT(g) FROM Guest g WHERE g.pgManagementOwner.pgId = :pgId " +
           "AND (g.guestStatus = 'FORMER' OR g.checkOutDate < CURRENT_DATE)")
    Long countFormerGuests(@Param("pgId") Long pgId);
}
