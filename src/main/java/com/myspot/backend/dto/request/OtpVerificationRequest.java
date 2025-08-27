package com.myspot.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationRequest {
    
    @NotBlank(message = "Email address is required")
    @Email(message = "Please provide a valid email address")
    private String emailAddress;
    
    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be exactly 6 digits")
    private String otp;
    
    @NotBlank(message = "OTP type is required")
    private String otpType; // EMAIL_VERIFICATION, LOGIN_VERIFICATION, PASSWORD_RESET
}