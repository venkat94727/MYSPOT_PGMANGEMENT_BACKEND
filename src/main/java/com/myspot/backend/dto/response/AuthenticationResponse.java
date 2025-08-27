package com.myspot.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse {

    private Boolean success;
    private String message;

    // JWT Token fields
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;

    // PG Management info
    private Long pgId;
    private String emailAddress;
    private String pgName;
    private String ownerName;

    // Status fields
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private String verificationStatus;
    private Boolean requiresOtpVerification;

    private LocalDateTime timestamp;
}