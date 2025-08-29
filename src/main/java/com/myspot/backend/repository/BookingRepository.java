
package com.myspot.backend.repository;

import com.myspot.backend.entities.Booking;
import com.myspot.backend.entities.PGManagementOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByPgManagementOwner(PGManagementOwner pgManagementOwner);
    List<Booking> findByPgManagementOwnerOrderByCreatedAtDesc(PGManagementOwner pgManagementOwner);
    List<Booking> findByPgManagementOwner_PgId(Long pgId);
    
    Optional<Booking> findByBookingReference(String bookingReference);
    
    List<Booking> findByPgManagementOwnerAndStatus(PGManagementOwner pgManagementOwner, Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.pgManagementOwner.pgId = :pgId AND b.status = :status ORDER BY b.createdAt DESC")
    List<Booking> findByPgIdAndStatus(@Param("pgId") Long pgId, @Param("status") Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.pgManagementOwner.pgId = :pgId " +
           "AND ((b.checkInDate <= :monthEnd AND b.checkOutDate >= :monthStart)) " +
           "ORDER BY b.checkInDate")
    List<Booking> findBookingsActiveInMonth(@Param("pgId") Long pgId, 
                                          @Param("monthStart") LocalDate monthStart, 
                                          @Param("monthEnd") LocalDate monthEnd);
    
    @Query("SELECT b FROM Booking b WHERE b.pgManagementOwner.pgId = :pgId " +
           "AND b.checkInDate >= :startDate AND b.checkInDate <= :endDate " +
           "ORDER BY b.checkInDate")
    List<Booking> findBookingsByCheckInRange(@Param("pgId") Long pgId, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.pgManagementOwner.pgId = :pgId " +
           "AND b.status = 'ACTIVE' " +
           "AND b.checkInDate <= CURRENT_DATE " +
           "AND (b.checkOutDate IS NULL OR b.checkOutDate >= CURRENT_DATE)")
    List<Booking> findCurrentActiveBookings(@Param("pgId") Long pgId);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.pgManagementOwner.pgId = :pgId")
    Long countByPgId(@Param("pgId") Long pgId);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.pgManagementOwner.pgId = :pgId AND b.status = :status")
    Long countByPgIdAndStatus(@Param("pgId") Long pgId, @Param("status") Booking.BookingStatus status);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.pgManagementOwner.pgId = :pgId " +
           "AND b.bookingDate >= :startDate AND b.bookingDate <= :endDate")
    Long countBookingsByDateRange(@Param("pgId") Long pgId, 
                                 @Param("startDate") LocalDate startDate, 
                                 @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.pgManagementOwner.pgId = :pgId " +
           "AND b.status IN ('CONFIRMED', 'ACTIVE', 'COMPLETED') " +
           "AND b.bookingDate >= :startDate AND b.bookingDate <= :endDate")
    java.math.BigDecimal sumRevenueByDateRange(@Param("pgId") Long pgId, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
}
