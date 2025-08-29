package com.myspot.backend.services;

import com.myspot.backend.dto.request.*;
import com.myspot.backend.dto.response.AuthenticationResponse;
import com.myspot.backend.dto.response.OtpResponse;
import com.myspot.backend.entities.PGManagementOwner;
import com.myspot.backend.repository.PGManagementRepository;
import com.myspot.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PGAuthService {

    private final PGManagementRepository pgManagementRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final OtpService otpService;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    public AuthenticationResponse registerPG(PGRegistrationRequest request, String clientIp, String userAgent) {
        log.info("Registering new PG Management: {}", request.getEmailAddress());
        validateRegistrationRequest(request);

        if (pgManagementRepository.existsByEmailAddress(request.getEmailAddress())) {
            throw new IllegalArgumentException("Email address already registered");
        }
        if (pgManagementRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        PGManagementOwner pg = createPGFromRequest(request);

        // Hash the raw password and set it explicitly before saving
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        pg.setPasswordHash(hashedPassword);

        pg = pgManagementRepository.save(pg);

        String otp = otpService.generateOtp();
        pg.setEmailOtp(otp);
        pg.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        pgManagementRepository.save(pg);

        emailService.sendOtpEmail(pg.getEmailAddress(), otp, pg.getOwnerName());

        return AuthenticationResponse.builder()
                .success(true)
                .message("Registration successful. Verify your email with the OTP sent.")
                .pgId(pg.getPgId())
                .emailAddress(pg.getEmailAddress())
                .pgName(pg.getPgName())
                .ownerName(pg.getOwnerName())
                .emailVerified(false)
                .requiresOtpVerification(true)
                .build();
    }

    public AuthenticationResponse login(PGLoginRequest request, String clientIp, String userAgent) {
        log.info("Login attempt for PG Management: {}", request.getEmailAddress());
        PGManagementOwner pg = pgManagementRepository.findByEmailAddress(request.getEmailAddress())
                .orElseThrow(() -> new UsernameNotFoundException("PG not found"));

        if (!pg.getIsActive()) {
            throw new BadCredentialsException("Account deactivated");
        }
        if (pg.isAccountLocked()) {
            throw new BadCredentialsException("Account locked");
        }
        if (!passwordEncoder.matches(request.getPassword(), pg.getPasswordHash())) {
            pg.incrementLoginAttempts();
            pgManagementRepository.save(pg);
            throw new BadCredentialsException("Invalid credentials");
        }

        pg.resetLoginAttempts();
        pg.setLastLogin(LocalDateTime.now());
        pgManagementRepository.save(pg);

        if (!pg.getEmailVerified()) {
            String otp = otpService.generateOtp();
            pg.setEmailOtp(otp);
            pg.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
            pgManagementRepository.save(pg);
            emailService.sendOtpEmail(pg.getEmailAddress(), otp, pg.getOwnerName());

            return AuthenticationResponse.builder()
                    .success(true)
                    .message("Verify your email OTP to complete login.")
                    .pgId(pg.getPgId())
                    .emailAddress(pg.getEmailAddress())
                    .pgName(pg.getPgName())
                    .ownerName(pg.getOwnerName())
                    .emailVerified(false)
                    .requiresOtpVerification(true)
                    .build();
        }

        String accessToken = jwtTokenProvider.createToken(request.getEmailAddress(), pg.getPgId().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(request.getEmailAddress());

        return AuthenticationResponse.builder()
                .success(true)
                .message("Login successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .pgId(pg.getPgId())
                .emailAddress(pg.getEmailAddress())
                .pgName(pg.getPgName())
                .ownerName(pg.getOwnerName())
                .emailVerified(pg.getEmailVerified())
                .phoneVerified(pg.getPhoneVerified())
                .verificationStatus(pg.getVerificationStatus().name())
                .requiresOtpVerification(false)
                .build();
    }

    public AuthenticationResponse verifyOtp(OtpVerificationRequest request, String clientIp, String userAgent) {
        log.info("OTP verification for PG: {}", request.getEmailAddress());
        PGManagementOwner pg = pgManagementRepository.findByEmailAddress(request.getEmailAddress())
                .orElseThrow(() -> new UsernameNotFoundException("PG not found"));

        if (!otpService.verifyOtp(pg.getEmailOtp(), request.getOtp()) || pg.isOtpExpired()) {
            pg.incrementOtpAttempts();
            pgManagementRepository.save(pg);
            throw new BadCredentialsException("Invalid or expired OTP");
        }

        pg.setEmailVerified(true);
        pg.setEmailOtp(null);
        pg.setOtpExpiryTime(null);
        pg.resetOtpAttempts();
        pg.setLastLogin(LocalDateTime.now());
        pgManagementRepository.save(pg);

        String accessToken = jwtTokenProvider.createToken(request.getEmailAddress(), pg.getPgId().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(request.getEmailAddress());

        return AuthenticationResponse.builder()
                .success(true)
                .message("Email verified; logged in.")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .pgId(pg.getPgId())
                .emailAddress(pg.getEmailAddress())
                .pgName(pg.getPgName())
                .ownerName(pg.getOwnerName())
                .emailVerified(true)
                .phoneVerified(pg.getPhoneVerified())
                .verificationStatus(pg.getVerificationStatus().name())
                .requiresOtpVerification(false)
                .build();
    }

    public OtpResponse resendOtp(ResendOtpRequest request) {
        log.info("Resending OTP for PG: {}", request.getEmailAddress());
        PGManagementOwner pg = pgManagementRepository.findByEmailAddress(request.getEmailAddress())
                .orElseThrow(() -> new UsernameNotFoundException("PG not found"));

        if (pg.getLastOtpRequest() != null &&
            pg.getLastOtpRequest().isAfter(LocalDateTime.now().minusMinutes(1))) {
            throw new IllegalArgumentException("Wait before requesting another OTP");
        }

        String otp = otpService.generateOtp();
        pg.setEmailOtp(otp);
        pg.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        pg.setLastOtpRequest(LocalDateTime.now());
        pgManagementRepository.save(pg);
        emailService.sendOtpEmail(pg.getEmailAddress(), otp, pg.getOwnerName());

        return OtpResponse.builder()
                .success(true)
                .message("OTP resent")
                .emailAddress(request.getEmailAddress())
                .expiryTime(pg.getOtpExpiryTime())
                .build();
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password for PG: {}", request.getEmailAddress());
        Optional<PGManagementOwner> opt = pgManagementRepository.findByEmailAddress(request.getEmailAddress());
        if (opt.isEmpty()) return;
        PGManagementOwner pg = opt.get();
        String token = otpService.generateResetToken();
        pg.setPasswordResetToken(token);
        pg.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
        pgManagementRepository.save(pg);
        emailService.sendPasswordResetEmail(pg.getEmailAddress(), token, pg.getOwnerName());
    }

    public void resetPassword(ResetPasswordRequest request) {
        log.info("Reset password attempt");
        PGManagementOwner pg = pgManagementRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (pg.isPasswordResetTokenExpired()) throw new IllegalArgumentException("Token expired");
        pg.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        pg.setPasswordResetToken(null);
        pg.setPasswordResetExpiry(null);
        pg.resetLoginAttempts();
        pgManagementRepository.save(pg);
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        String email = jwtTokenProvider.getEmailFromToken(request.getRefreshToken());
        PGManagementOwner pg = pgManagementRepository.findByEmailAddress(email)
                .orElseThrow(() -> new UsernameNotFoundException("PG not found"));
        String newAccess = jwtTokenProvider.createToken(email, pg.getPgId().toString());
        return AuthenticationResponse.builder()
                .success(true)
                .message("Token refreshed")
                .accessToken(newAccess)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .pgId(pg.getPgId())
                .emailAddress(email)
                .pgName(pg.getPgName())
                .ownerName(pg.getOwnerName())
                .build();
    }

    public void logout(String authHeader) {
        log.info("Logout PG");
        // Implement token invalidation if needed
    }

    public void verifyEmailByToken(String token) {
        log.info("Verify email by token");
        // Implement verification by token
    }

    public void verifyEmailByOtp(OtpVerificationRequest request) {
        verifyOtp(request, null, null);
    }

    public void sendVerificationEmail(EmailVerificationRequest request) {
        PGManagementOwner pg = pgManagementRepository.findByEmailAddress(request.getEmailAddress())
                .orElseThrow(() -> new UsernameNotFoundException("PG not found"));
        String otp = otpService.generateOtp();
        pg.setEmailOtp(otp);
        pg.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        pgManagementRepository.save(pg);
        emailService.sendOtpEmail(pg.getEmailAddress(), otp, pg.getOwnerName());
    }

    public boolean isEmailAvailable(String email) {
        return !pgManagementRepository.existsByEmailAddress(email);
    }

    public boolean isMobileAvailable(String mobile) {
        return !pgManagementRepository.existsByPhoneNumber(mobile);
    }

    // Helpers
    private void validateRegistrationRequest(PGRegistrationRequest request) {
        if (!request.isPasswordMatching()) throw new IllegalArgumentException("Passwords mismatch");
        if (!request.hasRequiredFields()) throw new IllegalArgumentException("Missing fields");
    }

    private PGManagementOwner createPGFromRequest(PGRegistrationRequest req) {
        return PGManagementOwner.builder()
                .pgName(req.getPgName())
                .ownerName(req.getOwnerName())
                .pgProfilePicture(req.getPgProfilePicture())
               
                
               
               
                .emailAddress(req.getEmailAddress().toLowerCase().trim())
                .phoneNumber(req.getPhoneNumber().trim())
                
              
                .city(req.getCity())
                .state(req.getState())
                .country(req.getCountry())
                .pincode(req.getPincode())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
               
                
                .isActive(true)
                .emailVerified(false)
                .phoneVerified(false)
                .verificationStatus(PGManagementOwner.VerificationStatus.PENDING)
                .build();
    }
}
