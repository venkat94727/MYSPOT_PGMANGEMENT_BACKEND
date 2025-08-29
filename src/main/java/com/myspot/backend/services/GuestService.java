
package com.myspot.backend.services;

import com.myspot.backend.entities.*;
import com.myspot.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GuestService {
    
    private final PGManagementOwnerRepository pgManagementOwnerRepository;
    private final GuestRepository guestRepository;
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllGuests(Long pgId, String status) {
        log.info("Getting all guests for PG ID: {}, status: {}", pgId, status);
        
        PGManagementOwner pgManagementOwner = pgManagementOwnerRepository.findById(pgId)
            .orElseThrow(() -> new RuntimeException("PG not found"));
        
        List<Guest> guests;
        
        if ("all".equals(status)) {
            guests = guestRepository.findByPgManagementOwnerOrderByCreatedAtDesc(pgManagementOwner);
        } else {
            try {
                Guest.GuestStatus guestStatus = Guest.GuestStatus.valueOf(status.toUpperCase());
                guests = guestRepository.findByPgManagementOwnerAndGuestStatus(pgManagementOwner, guestStatus);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid guest status: {}, using all guests", status);
                guests = guestRepository.findByPgManagementOwnerOrderByCreatedAtDesc(pgManagementOwner);
            }
        }
        
        return guests.stream()
            .map(this::convertGuestToMap)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getGuestDetails(Long pgId, Long guestId) {
        log.info("Getting guest details for guest ID: {} in PG ID: {}", guestId, pgId);
        
        Guest guest = guestRepository.findById(guestId)
            .orElseThrow(() -> new RuntimeException("Guest not found"));
        
        if (!guest.getPgManagementOwner().getPgId().equals(pgId)) {
            throw new RuntimeException("Guest does not belong to this PG");
        }
        
        return convertGuestToMap(guest);
    }
    
    public Map<String, Object> updateGuestStatus(Long pgId, Long guestId, String newStatus) {
        log.info("Updating guest status for guest ID: {} to status: {}", guestId, newStatus);
        
        Guest guest = guestRepository.findById(guestId)
            .orElseThrow(() -> new RuntimeException("Guest not found"));
        
        if (!guest.getPgManagementOwner().getPgId().equals(pgId)) {
            throw new RuntimeException("Guest does not belong to this PG");
        }
        
        try {
            Guest.GuestStatus status = Guest.GuestStatus.valueOf(newStatus.toUpperCase());
            guest.setGuestStatus(status);
            
            // Update active flag based on status
            guest.setIsActive(status == Guest.GuestStatus.ACTIVE);
            
            guest = guestRepository.save(guest);
            return convertGuestToMap(guest);
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid guest status: " + newStatus);
        }
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getGuestStats(Long pgId) {
        log.info("Getting guest statistics for PG ID: {}", pgId);
        
        Map<String, Object> stats = new HashMap<>();
        
        Long totalGuests = guestRepository.countByPgId(pgId);
        Long activeGuests = guestRepository.countByPgIdAndStatus(pgId, Guest.GuestStatus.ACTIVE);
        Long formerGuests = guestRepository.countFormerGuests(pgId);
        Long suspendedGuests = guestRepository.countByPgIdAndStatus(pgId, Guest.GuestStatus.SUSPENDED);
        
        stats.put("totalGuests", totalGuests != null ? totalGuests.intValue() : 0);
        stats.put("activeGuests", activeGuests != null ? activeGuests.intValue() : 0);
        stats.put("formerGuests", formerGuests != null ? formerGuests.intValue() : 0);
        stats.put("suspendedGuests", suspendedGuests != null ? suspendedGuests.intValue() : 0);
        
        // Calculate occupancy rate
        // This would depend on your PG capacity settings in PGDetails
        
        return stats;
    }
    
    private Map<String, Object> convertGuestToMap(Guest guest) {
        Map<String, Object> guestMap = new HashMap<>();
        
        guestMap.put("guestId", guest.getGuestId());
        guestMap.put("firstName", guest.getFirstName());
        guestMap.put("lastName", guest.getLastName());
        guestMap.put("fullName", guest.getFullName());
        guestMap.put("emailAddress", guest.getEmailAddress());
        guestMap.put("phoneNumber", guest.getPhoneNumber());
        guestMap.put("alternatePhone", guest.getAlternatePhone());
        guestMap.put("dateOfBirth", guest.getDateOfBirth());
        guestMap.put("gender", guest.getGender() != null ? guest.getGender().name() : null);
        guestMap.put("maritalStatus", guest.getMaritalStatus() != null ? guest.getMaritalStatus().name() : null);
        
        // Address information
        guestMap.put("permanentAddress", guest.getPermanentAddress());
        guestMap.put("permanentCity", guest.getPermanentCity());
        guestMap.put("permanentState", guest.getPermanentState());
        guestMap.put("permanentPincode", guest.getPermanentPincode());
        
        // Work information
        guestMap.put("occupation", guest.getOccupation());
        guestMap.put("companyName", guest.getCompanyName());
        guestMap.put("workAddress", guest.getWorkAddress());
        guestMap.put("monthlyIncome", guest.getMonthlyIncome());
        
        // Emergency contact
        guestMap.put("emergencyContactName", guest.getEmergencyContactName());
        guestMap.put("emergencyContactRelation", guest.getEmergencyContactRelation());
        guestMap.put("emergencyContactPhone", guest.getEmergencyContactPhone());
        
        // Documents
        guestMap.put("aadharNumber", guest.getAadharNumber());
        guestMap.put("panNumber", guest.getPanNumber());
        guestMap.put("photoPath", guest.getPhotoPath());
        
        // Room information
        guestMap.put("roomNumber", guest.getRoomNumber());
        guestMap.put("roomType", guest.getRoomType() != null ? guest.getRoomType().name() : null);
        guestMap.put("isAcRoom", guest.getIsAcRoom());
        guestMap.put("bedNumber", guest.getBedNumber());
        
        // Status and dates
        guestMap.put("guestStatus", guest.getGuestStatus().name());
        guestMap.put("isActive", guest.getIsActive());
        guestMap.put("checkInDate", guest.getCheckInDate());
        guestMap.put("checkOutDate", guest.getCheckOutDate());
        guestMap.put("expectedStayDuration", guest.getExpectedStayDuration());
        
        // Preferences and requirements
        guestMap.put("foodPreference", guest.getFoodPreference() != null ? guest.getFoodPreference().name() : null);
        guestMap.put("hasFoodService", guest.getHasFoodService());
        guestMap.put("specialRequirements", guest.getSpecialRequirements());
        guestMap.put("medicalConditions", guest.getMedicalConditions());
        guestMap.put("hasVehicle", guest.getHasVehicle());
        guestMap.put("vehicleDetails", guest.getVehicleDetails());
        
        // Financial information
        guestMap.put("securityDepositPaid", guest.getSecurityDepositPaid());
        guestMap.put("monthlyRent", guest.getMonthlyRent());
        guestMap.put("lastPaymentDate", guest.getLastPaymentDate());
        guestMap.put("nextPaymentDue", guest.getNextPaymentDue());
        
        // Timestamps
        guestMap.put("createdAt", guest.getCreatedAt());
        guestMap.put("updatedAt", guest.getUpdatedAt());
        
        // Calculated fields
        guestMap.put("age", guest.getAge());
        guestMap.put("isCurrentGuest", guest.isCurrentGuest());
        guestMap.put("isFormerGuest", guest.isFormerGuest());
        
        return guestMap;
    }
}
