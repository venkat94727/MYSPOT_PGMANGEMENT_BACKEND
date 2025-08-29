
package com.myspot.backend.services;

import com.myspot.backend.entities.PGDetails;
import com.myspot.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {
    
    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final ReviewRepository reviewRepository;
    private final PGDetailsRepository pgDetailsRepository;
    
    public Map<String, Object> getRevenueAnalytics(Long pgId, String month) {
        log.info("Getting revenue analytics for PG ID: {}, month: {}", pgId, month);
        
        Map<String, Object> analytics = new HashMap<>();
        
        LocalDate startDate;
        LocalDate endDate;
        
        if (!"all".equals(month)) {
            try {
                YearMonth yearMonth = YearMonth.parse(month);
                startDate = yearMonth.atDay(1);
                endDate = yearMonth.atEndOfMonth();
            } catch (DateTimeParseException e) {
                log.warn("Invalid month format: {}, using current month", month);
                YearMonth currentMonth = YearMonth.now();
                startDate = currentMonth.atDay(1);
                endDate = currentMonth.atEndOfMonth();
            }
        } else {
            // For "all", use current year
            startDate = LocalDate.now().withDayOfYear(1);
            endDate = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
        }
        
        BigDecimal totalRevenue = bookingRepository.sumRevenueByDateRange(pgId, startDate, endDate);
        Long totalBookings = bookingRepository.countBookingsByDateRange(pgId, startDate, endDate);
        
        analytics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        analytics.put("totalBookings", totalBookings != null ? totalBookings.intValue() : 0);
        analytics.put("averageBookingValue", 
            totalBookings != null && totalBookings > 0 && totalRevenue != null ? 
                totalRevenue.divide(BigDecimal.valueOf(totalBookings), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO);
        analytics.put("period", month);
        analytics.put("startDate", startDate.toString());
        analytics.put("endDate", endDate.toString());
        
        return analytics;
    }
    
    public Map<String, Object> getOccupancyAnalytics(Long pgId) {
        log.info("Getting occupancy analytics for PG ID: {}", pgId);
        
        Map<String, Object> analytics = new HashMap<>();
        
        var pgDetails = pgDetailsRepository.findActiveByPgId(pgId);
        
        if (pgDetails.isPresent()) {
            PGDetails details = pgDetails.get();
            
            analytics.put("totalCapacity", details.getTotalCapacity());
            analytics.put("currentOccupancy", details.getCurrentOccupancy());
            analytics.put("availableBeds", details.getAvailableBeds());
            analytics.put("occupancyRate", details.getOccupancyRate());
            
            // Room type breakdown
            Map<String, Object> roomBreakdown = new HashMap<>();
            roomBreakdown.put("singleSharing", Map.of(
                "total", details.getTotalSingleSharingRooms(),
                "remaining", details.getRemainingSingleSharingRooms()
            ));
            roomBreakdown.put("doubleSharing", Map.of(
                "total", details.getTotalDoubleSharingRooms(),
                "remaining", details.getRemainingDoubleSharingRooms()
            ));
            roomBreakdown.put("tripleSharing", Map.of(
                "total", details.getTotalTripleSharingRooms(),
                "remaining", details.getRemainingTripleSharingRooms()
            ));
            roomBreakdown.put("quadSharing", Map.of(
                "total", details.getTotalQuadSharingRooms(),
                "remaining", details.getRemainingQuadSharingRooms()
            ));
            
            analytics.put("roomBreakdown", roomBreakdown);
        } else {
            analytics.put("totalCapacity", 0);
            analytics.put("currentOccupancy", 0);
            analytics.put("availableBeds", 0);
            analytics.put("occupancyRate", BigDecimal.ZERO);
        }
        
        // Guest statistics
        Long activeGuests = guestRepository.countActiveGuests(pgId);
        Long formerGuests = guestRepository.countFormerGuests(pgId);
        
        analytics.put("activeGuests", activeGuests != null ? activeGuests.intValue() : 0);
        analytics.put("formerGuests", formerGuests != null ? formerGuests.intValue() : 0);
        
        return analytics;
    }
    
    public Map<String, Object> getMonthlyReport(Long pgId, String month) {
        log.info("Getting monthly report for PG ID: {}, month: {}", pgId, month);
        
        Map<String, Object> report = new HashMap<>();
        
        try {
            YearMonth yearMonth = YearMonth.parse(month);
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();
            
            // Revenue data
            BigDecimal totalRevenue = bookingRepository.sumRevenueByDateRange(pgId, monthStart, monthEnd);
            Long totalBookings = bookingRepository.countBookingsByDateRange(pgId, monthStart, monthEnd);
            
            report.put("month", month);
            report.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
            report.put("totalBookings", totalBookings != null ? totalBookings.intValue() : 0);
            
            // Booking status breakdown
            Map<String, Object> bookingBreakdown = new HashMap<>();
            bookingBreakdown.put("pending", bookingRepository.countByPgIdAndStatus(pgId, 
                com.myspot.backend.entities.Booking.BookingStatus.PENDING));
            bookingBreakdown.put("confirmed", bookingRepository.countByPgIdAndStatus(pgId, 
                com.myspot.backend.entities.Booking.BookingStatus.CONFIRMED));
            bookingBreakdown.put("active", bookingRepository.countByPgIdAndStatus(pgId, 
                com.myspot.backend.entities.Booking.BookingStatus.ACTIVE));
            bookingBreakdown.put("completed", bookingRepository.countByPgIdAndStatus(pgId, 
                com.myspot.backend.entities.Booking.BookingStatus.COMPLETED));
            bookingBreakdown.put("cancelled", bookingRepository.countByPgIdAndStatus(pgId, 
                com.myspot.backend.entities.Booking.BookingStatus.CANCELLED));
            
            report.put("bookingBreakdown", bookingBreakdown);
            
            // Occupancy data
            var pgDetails = pgDetailsRepository.findActiveByPgId(pgId);
            if (pgDetails.isPresent()) {
                report.put("occupancyRate", pgDetails.get().getOccupancyRate());
                report.put("totalCapacity", pgDetails.get().getTotalCapacity());
                report.put("currentOccupancy", pgDetails.get().getCurrentOccupancy());
            }
            
            // Review data
            Long totalReviews = reviewRepository.countActiveReviewsByPgId(pgId);
            BigDecimal averageRating = reviewRepository.getAverageRatingByPgId(pgId);
            
            report.put("totalReviews", totalReviews != null ? totalReviews.intValue() : 0);
            report.put("averageRating", averageRating != null ? averageRating : BigDecimal.ZERO);
            
        } catch (DateTimeParseException e) {
            log.error("Invalid month format: {}", month);
            throw new RuntimeException("Invalid month format: " + month);
        }
        
        return report;
    }
}
