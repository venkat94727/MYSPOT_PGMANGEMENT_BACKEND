package com.myspot.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Profile Update Request DTO
 * Used for updating user profile information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 100, message = "Owner name must be between 2 and 100 characters")
    private String ownerName;

    @NotBlank(message = "PG name is required")
    @Size(min = 2, max = 200, message = "PG name must be between 2 and 200 characters")
    private String pgName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Please provide a valid phone number")
    private String phoneNumber;

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    private String state;

    // Optional fields for future expansion
    private String country;
    private String pincode;
    private Double latitude;
    private Double longitude;

    /**
     * Validation method to check if required fields are present
     */
    public boolean hasRequiredFields() {
        return ownerName != null && !ownerName.trim().isEmpty() &&
               pgName != null && !pgName.trim().isEmpty() &&
               phoneNumber != null && !phoneNumber.trim().isEmpty() &&
               city != null && !city.trim().isEmpty() &&
               state != null && !state.trim().isEmpty();
    }

    /**
     * Sanitize input data
     */
    public void sanitize() {
        if (ownerName != null) this.ownerName = ownerName.trim();
        if (pgName != null) this.pgName = pgName.trim();
        if (phoneNumber != null) this.phoneNumber = phoneNumber.trim();
        if (city != null) this.city = city.trim();
        if (state != null) this.state = state.trim();
        if (country != null) this.country = country.trim();
        if (pincode != null) this.pincode = pincode.trim();
    }
}