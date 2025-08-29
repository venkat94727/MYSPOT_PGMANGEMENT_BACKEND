

package com.myspot.backend.controllers;

import com.myspot.backend.services.BookingService;
import com.myspot.backend.dto.response.ApiResponse;
import com.myspot.backend.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Booking Management", description = "Booking and reservation management APIs")
public class BookingController {
    
    private final BookingService bookingService;
    
    @GetMapping
    @Operation(summary = "Get all bookings", description = "Get all bookings with optional filters")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllBookings(
            @RequestParam(required = false, defaultValue = "all") String month,
            @RequestParam(required = false, defaultValue = "all") String status,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting all bookings for PG ID: {}, month: {}, status: {}", user.getId(), month, status);
        
        List<Map<String, Object>> bookings = bookingService.getAllBookings(user.getId(), month, status);
        
        return ResponseEntity.ok(ApiResponse.success(bookings, "Bookings retrieved successfully"));
    }
    
    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking details", description = "Get detailed information for a specific booking")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingDetails(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting booking details for booking ID: {} in PG ID: {}", bookingId, user.getId());
        
        Map<String, Object> booking = bookingService.getBookingDetails(user.getId(), bookingId);
        
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking details retrieved successfully"));
    }
    
    @GetMapping("/by-reference/{bookingReference}")
    @Operation(summary = "Get booking by reference", description = "Get booking details by booking reference number")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingByReference(
            @PathVariable String bookingReference,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting booking by reference: {} for PG ID: {}", bookingReference, user.getId());
        
        Map<String, Object> booking = bookingService.getBookingsByReference(user.getId(), bookingReference);
        
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking retrieved successfully"));
    }
    
    @PutMapping("/{bookingId}/status")
    @Operation(summary = "Update booking status", description = "Update the status of a booking")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        String newStatus = request.get("status");
        log.info("Updating booking status for booking ID: {} to status: {}", bookingId, newStatus);
        
        Map<String, Object> updatedBooking = bookingService.updateBookingStatus(user.getId(), bookingId, newStatus);
        
        return ResponseEntity.ok(ApiResponse.success(updatedBooking, "Booking status updated successfully"));
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get booking statistics", description = "Get booking statistics for dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingStats(
            @RequestParam(required = false, defaultValue = "all") String month,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting booking statistics for PG ID: {}, month: {}", user.getId(), month);
        
        Map<String, Object> stats = bookingService.getBookingStats(user.getId(), month);
        
        return ResponseEntity.ok(ApiResponse.success(stats, "Booking statistics retrieved successfully"));
    }
}