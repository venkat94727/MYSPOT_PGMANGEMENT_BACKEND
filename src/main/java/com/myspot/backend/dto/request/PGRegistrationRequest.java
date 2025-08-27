// src/main/java/com/myspot/backend/dto/request/PGRegistrationRequest.java
package com.myspot.backend.dto.request;

import com.myspot.backend.entities.PGManagement.PropertyType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * PG Management Registration Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PGRegistrationRequest {

    // Owner Details
    @NotBlank(message = "PG name is required")
    @Size(max = 200, message = "PG name must not exceed 200 characters")
    private String pgName;

    @NotBlank(message = "Owner name is required")
    @Size(max = 100, message = "Owner name must not exceed 100 characters")
    private String ownerName;

    @Size(max = 500, message = "Profile picture URL must not exceed 500 characters")
    private String pgProfilePicture;

   

   




    @NotBlank(message = "Email address is required")
    @Email(message = "Provide a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String emailAddress;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Provide a valid phone number")
    private String phoneNumber;

   

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be 8–50 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).*$",
             message = "Password must contain uppercase, lowercase, digit, special character")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
   

    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @DecimalMin(value = "-90.0", message = "Latitude must be ≥ -90")
    @DecimalMax(value = "90.0", message = "Latitude must be ≤ 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be ≥ -180")
    @DecimalMax(value = "180.0", message = "Longitude must be ≤ 180")
    private BigDecimal longitude;

    
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    public boolean hasRequiredFields() {
        return pgName != null && ownerName != null &&
               emailAddress != null && phoneNumber != null &&
               password != null  &&
               city != null && state != null && country != null && pincode != null
              ;
    }
}
