
package com.myspot.backend.services;

import com.myspot.backend.entities.*;
import com.myspot.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {
    
    private final PGManagementOwnerRepository pgManagementOwnerRepository;
    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllBookings(Long pgId, String month, String status) {
        log.info("Getting all bookings for PG ID: {}, month: {}, status: {}", pgId, month, status);
        
        PGManagementOwner pgManagementOwner = pgManagementOwnerRepository.findById(pgId)
            .orElseThrow(() -> new RuntimeException("PG not found"));
        
        List<Booking> bookings;
        
        // Filter by month if specified
        if (!"all".equals(month)) {
            try {
                YearMonth yearMonth = YearMonth.parse(month);
                LocalDate monthStart = yearMonth.atDay(1);
                LocalDate monthEnd = yearMonth.atEndOfMonth();
                bookings = bookingRepository.findBookingsActiveInMonth(pgId, monthStart, monthEnd);
            } catch (DateTimeParseException e) {
                log.warn("Invalid month format: {}, using all bookings", month);
                bookings = bookingRepository.findByPgManagementOwnerOrderByCreatedAtDesc(pgManagementOwner);
            }
        } else {
            bookings = bookingRepository.findByPgManagementOwnerOrderByCreatedAtDesc(pgManagementOwner);
        }
        
        // Filter by status if specified
        if (!"all".equals(status)) {
            try {
                Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
                bookings = bookings.stream()
                    .filter(b -> b.getStatus() == bookingStatus)
                    .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status: {}, ignoring status filter", status);
            }
        }
        
        return bookings.stream()
            .map(this::convertBookingToMap)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getBookingDetails(Long pgId, Long bookingId) {
        log.info("Getting booking details for booking ID: {} in PG ID: {}", bookingId, pgId);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getPgManagementOwner().getPgId().equals(pgId)) {
            throw new RuntimeException("Booking does not belong to this PG");
        }
        
        return convertBookingToMap(booking);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getBookingsByReference(Long pgId, String bookingReference) {
        log.info("Getting booking by reference: {} for PG ID: {}", bookingReference, pgId);
        
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
            .orElseThrow(() -> new RuntimeException("Booking not found with reference: " + bookingReference));
        
        if (!booking.getPgManagementOwner().getPgId().equals(pgId)) {
            throw new RuntimeException("Booking does not belong to this PG");
        }
        
        return convertBookingToMap(booking);
    }
    
    public Map<String, Object> updateBookingStatus(Long pgId, Long bookingId, String newStatus) {
        log.info("Updating booking status for booking ID: {} to status: {}", bookingId, newStatus);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getPgManagementOwner().getPgId().equals(pgId)) {
            throw new RuntimeException("Booking does not belong to this PG");
        }
        
        try {
            Booking.BookingStatus status = Booking.BookingStatus.valueOf(newStatus.toUpperCase());
            booking.setStatus(status);
            
            // Update payment status if needed
            if (status == Booking.BookingStatus.CONFIRMED) {
                booking.setPaymentStatus(Booking.PaymentStatus.ADVANCE_PAID);
            } else if (status == Booking.BookingStatus.COMPLETED) {
                booking.setPaymentStatus(Booking.PaymentStatus.PAID);
            }
            
            booking = bookingRepository.save(booking);
            return convertBookingToMap(booking);
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid booking status: " + newStatus);
        }
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getBookingStats(Long pgId, String month) {
        log.info("Getting booking statistics for PG ID: {}, month: {}", pgId, month);
        
        Map<String, Object> stats = new HashMap<>();
        
        Long totalBookings;
        Long pendingBookings;
        Long confirmedBookings;
        Long activeBookings;
        Long completedBookings;
        Long cancelledBookings;
        
        if (!"all".equals(month)) {
            try {
                YearMonth yearMonth = YearMonth.parse(month);
                LocalDate monthStart = yearMonth.atDay(1);
                LocalDate monthEnd = yearMonth.atEndOfMonth();
                totalBookings = bookingRepository.countBookingsByDateRange(pgId, monthStart, monthEnd);
                
                // For monthly stats, we need to filter by date and status
                List<Booking> monthlyBookings = bookingRepository.findBookingsActiveInMonth(pgId, monthStart, monthEnd);
                pendingBookings = monthlyBookings.stream()
                    .mapToLong(b -> b.getStatus() == Booking.BookingStatus.PENDING ? 1 : 0)
                    .sum();
                confirmedBookings = monthlyBookings.stream()
                    .mapToLong(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED ? 1 : 0)
                    .sum();
                activeBookings = monthlyBookings.stream()
                    .mapToLong(b -> b.getStatus() == Booking.BookingStatus.ACTIVE ? 1 : 0)
                    .sum();
                completedBookings = monthlyBookings.stream()
                    .mapToLong(b -> b.getStatus() == Booking.BookingStatus.COMPLETED ? 1 : 0)
                    .sum();
                cancelledBookings = monthlyBookings.stream()
                    .mapToLong(b -> b.getStatus() == Booking.BookingStatus.CANCELLED ? 1 : 0)
                    .sum();
            } catch (DateTimeParseException e) {
                log.warn("Invalid month format: {}, using all bookings", month);
                totalBookings = bookingRepository.countByPgId(pgId);
                pendingBookings = bookingRepository.countByPgIdAndStatus(pgId, Booking.BookingStatus.PENDING);
                confirmedBookings = bookingRepository.countByPgIdAndStatus(pgId, Booking.BookingStatus.CONFIRMED);
                activeBookings = bookingRepository.countByPgIdAndStatus(pgId, Booking.BookingStatus.ACTIVE);
                completedBookings = bookingRepository.countByPgIdAndStatus(pgId, Booking.BookingStatus.COMPLETED);
                cancelledBookings = bookingRepository.countByPgIdAndStatus(pgId, Booking.BookingStatus.CANCELLED);
            }
        } else {
            totalBookings = bookingRepository.countByPgId(pgId);
            pendingBookings = bookingRepository.countByPgIdAndStatus(pgId, Booking.BookingStatus.PENDING);
            confirmedBookings = bookingRepository.countByPgIdAndStatus(pgId, Booking.BookingStatus.CONFIRMED);
            activeBookings = bookingRepository.countByPgIdAndStatus(pgId, Booking.BookingStatus.ACTIVE);
            completedBookings = bookingRepository.countByPgIdAndStatus(pgId, Booking.BookingStatus.COMPLETED);
            cancelledBookings = bookingRepository.countByPgIdAndStatus(pgId, Booking.BookingStatus.CANCELLED);
        }
        
        stats.put("totalBookings", totalBookings != null ? totalBookings.intValue() : 0);
        stats.put("pendingBookings", pendingBookings != null ? pendingBookings.intValue() : 0);
        stats.put("confirmedBookings", confirmedBookings != null ? confirmedBookings.intValue() : 0);
        stats.put("activeBookings", activeBookings != null ? activeBookings.intValue() : 0);
        stats.put("completedBookings", completedBookings != null ? completedBookings.intValue() : 0);
        stats.put("cancelledBookings", cancelledBookings != null ? cancelledBookings.intValue() : 0);
        
        return stats;
    }
    
    private Map<String, Object> convertBookingToMap(Booking booking) {
        Map<String, Object> bookingMap = new HashMap<>();
        
        bookingMap.put("bookingId", booking.getBookingId());
        bookingMap.put("bookingReference", booking.getBookingReference());
        bookingMap.put("guestName", booking.getGuestName());
        bookingMap.put("contactNumber", booking.getContactNumber());
        bookingMap.put("emailAddress", booking.getEmailAddress());
        bookingMap.put("roomType", booking.getRoomType().name());
        bookingMap.put("roomNumber", booking.getRoomNumber());
        bookingMap.put("isAcRoom", booking.getIsAcRoom());
        bookingMap.put("bookingDate", booking.getBookingDate().toString());
        bookingMap.put("checkInDate", booking.getCheckInDate().toString());
        bookingMap.put("checkOutDate", booking.getCheckOutDate().toString());
        bookingMap.put("baseAmount", booking.getBaseAmount());
        bookingMap.put("totalAmount", booking.getTotalAmount());
        bookingMap.put("advancePaid", booking.getAdvancePaid());
        bookingMap.put("remainingAmount", booking.getRemainingAmount());
        bookingMap.put("status", booking.getStatus().name());
        bookingMap.put("paymentStatus", booking.getPaymentStatus().name());
        bookingMap.put("specialRequests", booking.getSpecialRequests());
        bookingMap.put("notes", booking.getNotes());
        bookingMap.put("durationDays", booking.getDurationDays());
        bookingMap.put("createdAt", booking.getCreatedAt());
        bookingMap.put("updatedAt", booking.getUpdatedAt());
        
        // Guest information
        if (booking.getGuest() != null) {
            Map<String, Object> guestInfo = new HashMap<>();
            guestInfo.put("guestId", booking.getGuest().getGuestId());
            guestInfo.put("fullName", booking.getGuest().getFullName());
            guestInfo.put("phoneNumber", booking.getGuest().getPhoneNumber());
            guestInfo.put("emailAddress", booking.getGuest().getEmailAddress());
            bookingMap.put("guest", guestInfo);
        }
        
        return bookingMap;
    }
}
