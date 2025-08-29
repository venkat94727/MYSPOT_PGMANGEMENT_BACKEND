
package com.myspot.backend.services;

import com.myspot.backend.entities.PGManagementOwner;
import com.myspot.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    
    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final ReviewRepository reviewRepository;
    private final PGDetailsRepository pgDetailsRepository;
    
    public Map<String, Object> getDashboardOverview(Long pgId) {
        log.info("Getting dashboard overview for PG ID: {}", pgId);
        
        Map<String, Object> dashboardData = new HashMap<>();
        
        Long totalGuests = guestRepository.countByPgId(pgId);
        Long activeBookings = bookingRepository.countByPgIdAndStatus(pgId, 
            com.myspot.backend.entities.Booking.BookingStatus.ACTIVE);
        Long formerGuests = guestRepository.countFormerGuests(pgId);
        
        dashboardData.put("totalGuests", totalGuests != null ? totalGuests.intValue() : 0);
        dashboardData.put("activeBookings", activeBookings != null ? activeBookings.intValue() : 0);
        dashboardData.put("formerGuests", formerGuests != null ? formerGuests.intValue() : 0);
        
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        
        BigDecimal monthlyRevenue = bookingRepository.sumRevenueByDateRange(pgId, monthStart, monthEnd);
        dashboardData.put("monthlyRevenue", monthlyRevenue != null ? 
            monthlyRevenue.divide(BigDecimal.valueOf(100000), 2, RoundingMode.HALF_UP) : 0.0);
        
        Map<String, Object> monthlyStats = getMonthlyStats(pgId, monthStart, monthEnd);
        dashboardData.put("monthlyStats", monthlyStats);
        
        return dashboardData;
    }
    
    public Map<String, Object> getMonthlyStats(Long pgId, String monthString) {
        YearMonth yearMonth = YearMonth.parse(monthString);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();
        
        return getMonthlyStats(pgId, monthStart, monthEnd);
    }
    
    private Map<String, Object> getMonthlyStats(Long pgId, LocalDate monthStart, LocalDate monthEnd) {
        Map<String, Object> stats = new HashMap<>();
        
        Long totalBookings = bookingRepository.countBookingsByDateRange(pgId, monthStart, monthEnd);
        Long completedBookings = bookingRepository.countByPgIdAndStatus(pgId, 
            com.myspot.backend.entities.Booking.BookingStatus.COMPLETED);
        Long cancelledBookings = bookingRepository.countByPgIdAndStatus(pgId, 
            com.myspot.backend.entities.Booking.BookingStatus.CANCELLED);
        Long pendingBookings = bookingRepository.countByPgIdAndStatus(pgId, 
            com.myspot.backend.entities.Booking.BookingStatus.PENDING);
        
        stats.put("totalBookings", totalBookings != null ? totalBookings.intValue() : 0);
        stats.put("completedBookings", completedBookings != null ? completedBookings.intValue() : 0);  
        stats.put("cancelledBookings", cancelledBookings != null ? cancelledBookings.intValue() : 0);
        stats.put("pendingBookings", pendingBookings != null ? pendingBookings.intValue() : 0);
        
        var pgDetails = pgDetailsRepository.findActiveByPgId(pgId);
        int occupancyRate = 0;
        if (pgDetails.isPresent() && pgDetails.get().getTotalCapacity() > 0) {
            int currentOccupancy = pgDetails.get().getCurrentOccupancy() != null ? 
                pgDetails.get().getCurrentOccupancy() : 0;
            int totalCapacity = pgDetails.get().getTotalCapacity();
            occupancyRate = (int) Math.round((double) currentOccupancy / totalCapacity * 100);
        }
        stats.put("occupancyRate", occupancyRate);
        
        return stats;
    }
}
