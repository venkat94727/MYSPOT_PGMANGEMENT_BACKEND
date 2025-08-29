package com.myspot.backend.controllers;

import com.myspot.backend.dto.request.*;
import com.myspot.backend.dto.response.ApiResponse;
import com.myspot.backend.dto.response.AuthenticationResponse;
import com.myspot.backend.dto.response.OtpResponse;
import com.myspot.backend.services.PGAuthService;
import com.myspot.backend.dto.request.OtpVerificationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/pg-auth")
@RequiredArgsConstructor
@Tag(name = "PG Authentication", description = "PG Management authentication and account management APIs")
public class PGAuthController {

    private final PGAuthService pgAuthService;

    // ============ REGISTRATION ENDPOINT ============
    @PostMapping("/register")
    @Operation(summary = "Register new PG Management",
               description = "Register a new PG Management account with email and mobile verification")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> registerPG(
            @Valid @RequestBody PGRegistrationRequest request,
            HttpServletRequest httpRequest) {

        log.info("PG Management registration attempt for email: {}", request.getEmailAddress());

        String clientIp = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthenticationResponse response = pgAuthService.registerPG(request, clientIp, userAgent);

        log.info("PG Management registration successful for email: {}", request.getEmailAddress());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registration successful! Please check your email for OTP verification."));
    }

    // ============ LOGIN ENDPOINT ============
    @PostMapping("/login")
    @Operation(summary = "Login PG Management",
               description = "Authenticate PG Management with email and password")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody PGLoginRequest request,
            HttpServletRequest httpRequest) {

        log.info("PG Management login attempt for email: {}", request.getEmailAddress());

        AuthenticationResponse response = pgAuthService.login(request, getClientIp(httpRequest), getUserAgent(httpRequest));

        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    // ============ OTP VERIFICATION ENDPOINT ============
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP",
               description = "Verify OTP for login or email verification and complete authentication")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> verifyOtp(
            @Valid @RequestBody OtpVerificationRequest request,
            HttpServletRequest httpRequest) {

        log.info("OTP verification attempt for email: {}, type: {}", request.getEmailAddress(), request.getOtpType());

        String clientIp = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthenticationResponse response = pgAuthService.verifyOtp(request, clientIp, userAgent);

        return ResponseEntity.ok(ApiResponse.success(response, "OTP verified successfully! You are now logged in."));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP",
               description = "Resend OTP code to PG Management email")
    public ResponseEntity<ApiResponse<OtpResponse>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {

        log.info("OTP resend request for email: {}, type: {}", request.getEmailAddress(), request.getOtpType());

        OtpResponse response = pgAuthService.resendOtp(request);

        return ResponseEntity.ok(ApiResponse.success(response, "OTP resent successfully! Please check your email."));
    }

    // ============ PASSWORD MANAGEMENT ENDPOINT ============
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password",
               description = "Send password reset link to PG Management email")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        log.info("Password reset request for email: {}", request.getEmailAddress());

        pgAuthService.forgotPassword(request);

        return ResponseEntity.ok(ApiResponse.success("Reset email sent", "Password reset instructions sent to your email address."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password",
               description = "Reset password using reset token")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        log.info("Password reset attempt with token");

        pgAuthService.resetPassword(request);

        return ResponseEntity.ok(ApiResponse.success("Password reset", "Your password has been reset successfully."));
    }

    // ============ TOKEN MANAGEMENT ENDPOINT ============
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh JWT token",
               description = "Generate new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("Token refresh request");

        AuthenticationResponse response = pgAuthService.refreshToken(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully."));
    }

    @PostMapping("/logout")
    @Operation(summary = "PG Management logout",
               description = "Logout PG Management and invalidate tokens")
    public ResponseEntity<ApiResponse<String>> logout(
            @Parameter(description = "Authorization header with Bearer token")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Logout request");
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Authorization header missing"));
        }
        pgAuthService.logout(authHeader);
        return ResponseEntity.ok(ApiResponse.success("Logged out", "You have been logged out successfully."));
    }


    // ============ EMAIL VERIFICATION ENDPOINT ============
    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address",
               description = "Verify PG Management email using verification token or OTP")
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @RequestParam(required = false) String token,
            @Valid @RequestBody(required = false) OtpVerificationRequest otpRequest) {

        if (token != null) {
            pgAuthService.verifyEmailByToken(token);
            return ResponseEntity.ok(ApiResponse.success("Email verified", "Your email has been verified successfully."));
        } else if (otpRequest != null) {
            pgAuthService.verifyEmailByOtp(otpRequest);
            return ResponseEntity.ok(ApiResponse.success("Email verified", "Your email has been verified successfully."));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid request", "Either token or OTP is required for email verification."));
        }
    }

    @PostMapping("/send-verification-email")
    @Operation(summary = "Send verification email",
               description = "Send verification email to PG Management")
    public ResponseEntity<ApiResponse<String>> sendVerificationEmail(
            @Valid @RequestBody EmailVerificationRequest request) {

        log.info("Verification email request for: {}", request.getEmailAddress());

        pgAuthService.sendVerificationEmail(request);

        return ResponseEntity.ok(ApiResponse.success("Verification email sent", "Please check your email for verification instructions."));
    }

    // ============ ACCOUNT STATUS ENDPOINT ============
    @GetMapping("/check-email")
    @Operation(summary = "Check email availability",
               description = "Check if email address is available for registration")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(
            @RequestParam String email) {

        boolean available = pgAuthService.isEmailAvailable(email);
        String message = available ? "Email is available" : "Email is already registered";

        return ResponseEntity.ok(ApiResponse.success(available, message));
    }

    @GetMapping("/check-mobile")
    @Operation(summary = "Check mobile availability",
               description = "Check if mobile number is available for registration")
    public ResponseEntity<ApiResponse<Boolean>> checkMobileAvailability(
            @RequestParam String mobile) {

        boolean available = pgAuthService.isMobileAvailable(mobile);
        String message = available ? "Mobile number is available" : "Mobile number is already registered";

        return ResponseEntity.ok(ApiResponse.success(available, message));
    }

    // ============ UTILITY METHODS ============
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private String getClientIp(HttpServletRequest request) {
        return getClientIpAddress(request);
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
