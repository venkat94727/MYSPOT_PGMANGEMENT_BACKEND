
package com.myspot.backend.services;

import com.myspot.backend.entities.*;
import com.myspot.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PGService {
    
    private final PGManagementOwnerRepository pgManagementOwnerRepository;
    private final PGDetailsRepository pgDetailsRepository;
    private final PGImageRepository pgImageRepository;
    private final AmenityRepository amenityRepository;
    private final FoodServiceRepository foodServiceRepository;
    private final RuleRestrictionRepository ruleRestrictionRepository;
    private final ExtraChargeRepository extraChargeRepository;
    private final FileUploadService fileUploadService;
    
    @Transactional(readOnly = true)
    public Map<String, Object> getPGDetails(Long pgId) {
        log.info("Getting PG details for PG ID: {}", pgId);
        
        PGManagementOwner pgManagementOwner = pgManagementOwnerRepository.findById(pgId)
            .orElseThrow(() -> new RuntimeException("PG not found"));
        
        PGDetails pgDetails = pgDetailsRepository.findByPgManagementOwner(pgManagementOwner)
            .orElseGet(() -> createDefaultPGDetails(pgManagementOwner));
        
        return buildPGDetailsResponse(pgManagementOwner, pgDetails);
    }
    
    public Map<String, Object> updatePGDetails(Long pgId, Map<String, Object> updateData) {
        log.info("Updating PG details for PG ID: {}", pgId);
        
        PGManagementOwner pgManagementOwner = pgManagementOwnerRepository.findById(pgId)
            .orElseThrow(() -> new RuntimeException("PG not found"));
        
        PGDetails pgDetails = pgDetailsRepository.findByPgManagementOwner(pgManagementOwner)
            .orElseGet(() -> createDefaultPGDetails(pgManagementOwner));
        
        updateBasicInfo(pgDetails, updateData);
        updateAddressInfo(pgDetails, updateData);
        updateContactInfo(pgDetails, updateData);
        updateRoomTypes(pgDetails, updateData);
        updateTimings(pgDetails, updateData);
        updateRatings(pgDetails, updateData);
        
        pgDetails = pgDetailsRepository.save(pgDetails);
        updateRelatedEntities(pgDetails, updateData);
        
        return buildPGDetailsResponse(pgManagementOwner, pgDetails);
    }
    
    public String uploadProfilePicture(Long pgId, MultipartFile file) {
        log.info("Uploading profile picture for PG ID: {}", pgId);
        
        PGManagementOwner pgManagementOwner = pgManagementOwnerRepository.findById(pgId)
            .orElseThrow(() -> new RuntimeException("PG not found"));
        
        String imageUrl = fileUploadService.uploadImage(file, "pg-profiles");
        
        pgManagementOwner.setPgProfilePicture(imageUrl);
        pgManagementOwnerRepository.save(pgManagementOwner);
        
        Optional<PGDetails> pgDetails = pgDetailsRepository.findByPgManagementOwner(pgManagementOwner);
        if (pgDetails.isPresent()) {
            pgImageRepository.findProfilePictureByPgDetailsId(pgDetails.get().getPgDetailsId())
                .ifPresent(oldImage -> {
                    oldImage.setIsProfilePicture(false);
                    pgImageRepository.save(oldImage);
                });
            
            PGImage profileImage = PGImage.builder()
                .pgDetails(pgDetails.get())
                .imageUrl(imageUrl)
                .imagePath(imageUrl)
                .originalFilename(file.getOriginalFilename())
                .imageType(PGImage.ImageType.PROFILE)
                .isProfilePicture(true)
                .isActive(true)
                .displayOrder(0)
                .build();
            
            pgImageRepository.save(profileImage);
        }
        
        return imageUrl;
    }
    
    public List<Map<String, Object>> uploadPGPictures(Long pgId, List<MultipartFile> files) {
        log.info("Uploading {} PG pictures for PG ID: {}", files.size(), pgId);
        
        PGManagementOwner pgManagementOwner = pgManagementOwnerRepository.findById(pgId)
            .orElseThrow(() -> new RuntimeException("PG not found"));
        
        PGDetails pgDetails = pgDetailsRepository.findByPgManagementOwner(pgManagementOwner)
            .orElseGet(() -> createDefaultPGDetails(pgManagementOwner));
        
     // Replace the return statement in uploadPGPictures method:
        return files.stream().map(file -> {
            String imageUrl = fileUploadService.uploadImage(file, "pg-pictures");
            
            PGImage pgImage = PGImage.builder()
                .pgDetails(pgDetails)
                .imageUrl(imageUrl)
                .imagePath(imageUrl)
                .originalFilename(file.getOriginalFilename())
                .imageType(PGImage.ImageType.GENERAL)
                .isProfilePicture(false)
                .isActive(true)
                .displayOrder(0)
                .build();
            
            pgImage = pgImageRepository.save(pgImage);
            
            Map<String, Object> map = new HashMap<>();
            map.put("id", pgImage.getImageId());
            map.put("imageUrl", pgImage.getImageUrl());
            map.put("originalFilename", pgImage.getOriginalFilename());
            map.put("description", pgImage.getDescription() != null ? pgImage.getDescription() : "");
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }
    
    public void updatePictureDescription(Long pgId, Long imageId, String description) {
        log.info("Updating picture description for image ID: {} in PG ID: {}", imageId, pgId);
        
        PGImage pgImage = pgImageRepository.findById(imageId)
            .orElseThrow(() -> new RuntimeException("Image not found"));
        
        pgImage.setDescription(description);
        pgImageRepository.save(pgImage);
    }
    
    public void deletePGPicture(Long pgId, Long imageId) {
        log.info("Deleting PG picture ID: {} for PG ID: {}", imageId, pgId);
        
        PGImage pgImage = pgImageRepository.findById(imageId)
            .orElseThrow(() -> new RuntimeException("Image not found"));
        
        fileUploadService.deleteFile(pgImage.getImagePath());
        pgImageRepository.delete(pgImage);
    }
    
    // Helper methods
    private PGDetails createDefaultPGDetails(PGManagementOwner pgManagementOwner) {
        return PGDetails.builder()
            .pgManagementOwner(pgManagementOwner)
            .pgDescription("")
            .pgType("Economy")
            .genderPreference("Mixed")
            .isActive(true)
            .isVerified(false)
            .isFeatured(false)
            .totalCapacity(0)
            .currentOccupancy(0)
            .availableBeds(0)
            .waitingListCount(0)
            .totalReviews(0)
            .build();
    }
    
    private Map<String, Object> buildPGDetailsResponse(PGManagementOwner pgManagementOwner, PGDetails pgDetails) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("pgId", pgManagementOwner.getPgId());
        response.put("pgName", pgManagementOwner.getPgName());
        response.put("ownerName", pgManagementOwner.getOwnerName());
        response.put("pgProfilePicture", pgManagementOwner.getPgProfilePicture());
        response.put("emailAddress", pgManagementOwner.getEmailAddress());
        response.put("phoneNumber", pgManagementOwner.getPhoneNumber());
        response.put("city", pgManagementOwner.getCity());
        response.put("state", pgManagementOwner.getState());
        response.put("country", pgManagementOwner.getCountry());
        response.put("pincode", pgManagementOwner.getPincode());
        response.put("latitude", pgManagementOwner.getLatitude());
        response.put("longitude", pgManagementOwner.getLongitude());
        
        if (pgDetails != null) {
            response.put("pgDescription", pgDetails.getPgDescription());
            response.put("pgType", pgDetails.getPgType());
            response.put("genderPreference", pgDetails.getGenderPreference());
            response.put("establishedYear", pgDetails.getEstablishedYear());
            response.put("panNumber", pgDetails.getPanNumber());
            
            // Address
            Map<String, Object> address = new HashMap<>();
            address.put("addressLine1", pgDetails.getAddressLine1());
            address.put("addressLine2", pgDetails.getAddressLine2());
            address.put("locality", pgDetails.getLocality());
            response.put("address", address);
            
            // Contact
            Map<String, Object> contact = new HashMap<>();
            contact.put("contactPersonName", pgDetails.getContactPersonName());
            contact.put("contactMobileNumber", pgDetails.getContactMobileNumber());
            contact.put("emergencyContactNumber", pgDetails.getEmergencyContactNumber());
            contact.put("landlineNumber", pgDetails.getLandlineNumber());
            contact.put("whatsappNumber", pgDetails.getWhatsappNumber());
            response.put("contact", contact);
            
            // Room types
            Map<String, Object> roomTypes = new HashMap<>();
            roomTypes.put("singleSharingAvailable", pgDetails.getSingleSharingAvailable());
            roomTypes.put("doubleSharingAvailable", pgDetails.getDoubleSharingAvailable());
            roomTypes.put("tripleSharingAvailable", pgDetails.getTripleSharingAvailable());
            roomTypes.put("quadSharingAvailable", pgDetails.getQuadSharingAvailable());
            roomTypes.put("singleSharingCost", pgDetails.getSingleSharingCost());
            roomTypes.put("doubleSharingCostPerPerson", pgDetails.getDoubleSharingCostPerPerson());
            roomTypes.put("tripleSharingCostPerPerson", pgDetails.getTripleSharingCostPerPerson());
            roomTypes.put("quadSharingCostPerPerson", pgDetails.getQuadSharingCostPerPerson());
            response.put("roomTypes", roomTypes);
            
            // Timings
            Map<String, Object> timings = new HashMap<>();
            timings.put("checkInTime", pgDetails.getCheckInTime());
            timings.put("checkOutTime", pgDetails.getCheckOutTime());
            response.put("timings", timings);
            
            // Ratings
            Map<String, Object> ratings = new HashMap<>();
            ratings.put("overallRating", pgDetails.getOverallRating());
            ratings.put("totalReviews", pgDetails.getTotalReviews());
            ratings.put("cleanlinessRating", pgDetails.getCleanlinessRating());
            ratings.put("locationRating", pgDetails.getLocationRating());
            response.put("ratings", ratings);
            
            response.put("pgPictures", getPGPictures(pgDetails));
            response.put("amenities", getAmenities(pgDetails));
            response.put("foodServices", getFoodServices(pgDetails));
            response.put("rulesRestrictions", getRulesRestrictions(pgDetails));
            response.put("extraCharges", getExtraCharges(pgDetails));
        }
        
        return response;
    }
    
    private List<Map<String, Object>> getPGPictures(PGDetails pgDetails) {
        return pgImageRepository.findActiveImagesByPgDetailsId(pgDetails.getPgDetailsId())
            .stream()
            .map(image -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", image.getImageId());
                map.put("imageUrl", image.getImageUrl());
                map.put("description", image.getDescription() != null ? image.getDescription() : "");
                map.put("fileName", image.getOriginalFilename() != null ? image.getOriginalFilename() : "");
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    private List<Map<String, Object>> getAmenities(PGDetails pgDetails) {
        return amenityRepository.findByPgDetails(pgDetails)
            .stream()
            .map(amenity -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", amenity.getAmenityId());
                map.put("category", amenity.getCategory());
                map.put("name", amenity.getName());
                map.put("description", amenity.getDescription() != null ? amenity.getDescription() : "");
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    private List<Map<String, Object>> getFoodServices(PGDetails pgDetails) {
        return foodServiceRepository.findByPgDetails(pgDetails)
            .stream()
            .map(service -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", service.getFoodServiceId());
                map.put("serviceType", service.getServiceType());
                map.put("timing", service.getTiming());
                map.put("cost", service.getCost());
                map.put("description", service.getDescription() != null ? service.getDescription() : "");
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    private List<Map<String, Object>> getRulesRestrictions(PGDetails pgDetails) {
        return ruleRestrictionRepository.findByPgDetailsOrderByDisplayOrderAsc(pgDetails)
            .stream()
            .map(rule -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rule.getRuleId());
                map.put("ruleType", rule.getRuleType());
                map.put("description", rule.getDescription());
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    private List<Map<String, Object>> getExtraCharges(PGDetails pgDetails) {
        return extraChargeRepository.findByPgDetails(pgDetails)
            .stream()
            .map(charge -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", charge.getChargeId());
                map.put("chargeType", charge.getChargeType());
                map.put("amount", charge.getAmount());
                map.put("description", charge.getDescription() != null ? charge.getDescription() : "");
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    
    // Update helper methods
    private void updateBasicInfo(PGDetails pgDetails, Map<String, Object> data) {
        if (data.containsKey("pgDescription")) {
            pgDetails.setPgDescription((String) data.get("pgDescription"));
        }
        if (data.containsKey("pgType")) {
            pgDetails.setPgType((String) data.get("pgType"));
        }
        if (data.containsKey("genderPreference")) {
            pgDetails.setGenderPreference((String) data.get("genderPreference"));
        }
    }
    
    private void updateAddressInfo(PGDetails pgDetails, Map<String, Object> data) {
        if (data.containsKey("addressLine1")) {
            pgDetails.setAddressLine1((String) data.get("addressLine1"));
        }
        if (data.containsKey("addressLine2")) {
            pgDetails.setAddressLine2((String) data.get("addressLine2"));
        }
        if (data.containsKey("locality")) {
            pgDetails.setLocality((String) data.get("locality"));
        }
    }
    
    private void updateContactInfo(PGDetails pgDetails, Map<String, Object> data) {
        if (data.containsKey("contactPersonName")) {
            pgDetails.setContactPersonName((String) data.get("contactPersonName"));
        }
    }
    
    @SuppressWarnings("unchecked")
    private void updateRoomTypes(PGDetails pgDetails, Map<String, Object> data) {
        if (data.containsKey("roomTypes")) {
            Map<String, Object> roomTypes = (Map<String, Object>) data.get("roomTypes");
            
            if (roomTypes.containsKey("singleSharingAvailable")) {
                pgDetails.setSingleSharingAvailable((Boolean) roomTypes.get("singleSharingAvailable"));
            }
            if (roomTypes.containsKey("singleSharingCost")) {
                Object cost = roomTypes.get("singleSharingCost");
                pgDetails.setSingleSharingCost(cost instanceof String ? 
                    new BigDecimal((String) cost) : (BigDecimal) cost);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void updateTimings(PGDetails pgDetails, Map<String, Object> data) {
        if (data.containsKey("timings")) {
            Map<String, Object> timings = (Map<String, Object>) data.get("timings");
            
            if (timings.containsKey("checkInTime")) {
                pgDetails.setCheckInTime((String) timings.get("checkInTime"));
            }
            if (timings.containsKey("checkOutTime")) {
                pgDetails.setCheckOutTime((String) timings.get("checkOutTime"));
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void updateRatings(PGDetails pgDetails, Map<String, Object> data) {
        if (data.containsKey("ratings")) {
            Map<String, Object> ratings = (Map<String, Object>) data.get("ratings");
            
            if (ratings.containsKey("overallRating")) {
                Object rating = ratings.get("overallRating");
                pgDetails.setOverallRating(rating instanceof Double ? 
                    BigDecimal.valueOf((Double) rating) : (BigDecimal) rating);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void updateRelatedEntities(PGDetails pgDetails, Map<String, Object> data) {
        if (data.containsKey("amenities")) {
            List<Map<String, Object>> amenitiesData = (List<Map<String, Object>>) data.get("amenities");
            updateAmenities(pgDetails, amenitiesData);
        }
        
        if (data.containsKey("foodServices")) {
            List<Map<String, Object>> foodServicesData = (List<Map<String, Object>>) data.get("foodServices");
            updateFoodServices(pgDetails, foodServicesData);
        }
        
        if (data.containsKey("rulesRestrictions")) {
            List<Map<String, Object>> rulesData = (List<Map<String, Object>>) data.get("rulesRestrictions");
            updateRulesRestrictions(pgDetails, rulesData);
        }
        
        if (data.containsKey("extraCharges")) {
            List<Map<String, Object>> chargesData = (List<Map<String, Object>>) data.get("extraCharges");
            updateExtraCharges(pgDetails, chargesData);
        }
    }
    
    private void updateAmenities(PGDetails pgDetails, List<Map<String, Object>> amenitiesData) {
        amenityRepository.deleteAll(amenityRepository.findByPgDetails(pgDetails));
        
        amenitiesData.forEach(amenityData -> {
            Amenity amenity = Amenity.builder()
                .pgDetails(pgDetails)
                .category((String) amenityData.get("category"))
                .name((String) amenityData.get("name"))
                .description((String) amenityData.get("description"))
                .isAvailable(true)
                .isFree(true)
                .build();
            amenityRepository.save(amenity);
        });
    }
    
    private void updateFoodServices(PGDetails pgDetails, List<Map<String, Object>> foodServicesData) {
        foodServiceRepository.deleteAll(foodServiceRepository.findByPgDetails(pgDetails));
        
        foodServicesData.forEach(serviceData -> {
            Object costObj = serviceData.get("cost");
            BigDecimal cost = costObj instanceof String ? 
                new BigDecimal((String) costObj) : (BigDecimal) costObj;
            
            FoodService service = FoodService.builder()
                .pgDetails(pgDetails)
                .serviceType((String) serviceData.get("serviceType"))
                .timing((String) serviceData.get("timing"))
                .cost(cost)
                .description((String) serviceData.get("description"))
                .isAvailable(true)
                .build();
            foodServiceRepository.save(service);
        });
    }
    
    private void updateRulesRestrictions(PGDetails pgDetails, List<Map<String, Object>> rulesData) {
        ruleRestrictionRepository.deleteAll(ruleRestrictionRepository.findByPgDetails(pgDetails));
        
        for (int i = 0; i < rulesData.size(); i++) {
            Map<String, Object> ruleData = rulesData.get(i);
            RuleRestriction rule = RuleRestriction.builder()
                .pgDetails(pgDetails)
                .ruleType((String) ruleData.get("ruleType"))
                .description((String) ruleData.get("description"))
                .isActive(true)
                .displayOrder(i)
                .build();
            ruleRestrictionRepository.save(rule);
        }
    }
    
    private void updateExtraCharges(PGDetails pgDetails, List<Map<String, Object>> chargesData) {
        extraChargeRepository.deleteAll(extraChargeRepository.findByPgDetails(pgDetails));
        
        chargesData.forEach(chargeData -> {
            Object amountObj = chargeData.get("amount");
            BigDecimal amount = amountObj instanceof String ? 
                new BigDecimal((String) amountObj) : (BigDecimal) amountObj;
            
            ExtraCharge charge = ExtraCharge.builder()
                .pgDetails(pgDetails)
                .chargeType((String) chargeData.get("chargeType"))
                .amount(amount)
                .description((String) chargeData.get("description"))
                .isActive(true)
                .build();
            extraChargeRepository.save(charge);
        });
    }
}