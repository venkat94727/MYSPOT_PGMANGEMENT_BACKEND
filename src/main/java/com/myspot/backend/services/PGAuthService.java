package com.myspot.backend.services;
import com.myspot.backend.dto.request.ProfileUpdateRequest;
import com.myspot.backend.dto.request.ChangePasswordRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.myspot.backend.dto.request.*;
import com.myspot.backend.dto.response.AuthenticationResponse;
import com.myspot.backend.dto.response.OtpResponse;
import com.myspot.backend.entities.PGManagementOwner;
import com.myspot.backend.repository.PGManagementOwnerRepository;
import com.myspot.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PGAuthService {

    private final PGManagementOwnerRepository pgManagementRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final OtpService otpService;
    private final FileStorageService fileStorageService;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;
    

public AuthenticationResponse registerPG(PGRegistrationRequest request, 
                                        MultipartFile profilePicture, 
                                        String clientIp, 
                                        String userAgent) {
    log.info("Registering new PG Management: {}", request.getEmailAddress());
    validateRegistrationRequest(request);

    if (pgManagementRepository.existsByEmailAddress(request.getEmailAddress())) {
        throw new IllegalArgumentException("Email address already registered");
    }
    if (pgManagementRepository.existsByPhoneNumber(request.getPhoneNumber())) {
        throw new IllegalArgumentException("Phone number already registered");
    }

    // Handle profile picture upload
    String profilePictureFileName = null;
    if (profilePicture != null && !profilePicture.isEmpty()) {
        try {
            // Generate prefix for the file (pg + email hash)
            String prefix = "pg_" + Math.abs(request.getEmailAddress().hashCode());
            profilePictureFileName = fileStorageService.storeFile(profilePicture, prefix);
            log.info("Profile picture uploaded successfully: {}", profilePictureFileName);
        } catch (Exception e) {
            log.error("Failed to upload profile picture for: {}", request.getEmailAddress(), e);
            throw new IllegalArgumentException("Failed to upload profile picture: " + e.getMessage());
        }
    }

    PGManagementOwner pg = createPGFromRequest(request, profilePictureFileName);

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
            .profilePictureUrl(profilePictureFileName != null ? 
                fileStorageService.generateFileUrl(profilePictureFileName) : null)
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

    private PGManagementOwner createPGFromRequest(PGRegistrationRequest req, String profilePictureFileName) {
        return PGManagementOwner.builder()
                .pgName(req.getPgName())
                .ownerName(req.getOwnerName())
                .pgProfilePicture(profilePictureFileName) // Store filename instead of URL
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
 // ADD these methods to your PGAuthService class:

   
    public Map<String, Object> updateProfile(Long pgId, Map<String, Object> updateData) {
        log.info("Updating profile for PG ID: {}", pgId);
        
        PGManagementOwner pgManagementOwner = pgManagementRepository.findById(pgId)
            .orElseThrow(() -> new UsernameNotFoundException("PG not found"));
        
        if (updateData.containsKey("pgName")) {
            pgManagementOwner.setPgName((String) updateData.get("pgName"));
        }
        if (updateData.containsKey("ownerName")) {
            pgManagementOwner.setOwnerName((String) updateData.get("ownerName"));
        }
        if (updateData.containsKey("phoneNumber")) {
            pgManagementOwner.setPhoneNumber((String) updateData.get("phoneNumber"));
        }
        if (updateData.containsKey("city")) {
            pgManagementOwner.setCity((String) updateData.get("city"));
        }
        
        pgManagementOwner = pgManagementRepository.save(pgManagementOwner);
        
        return getCurrentUserProfile(pgManagementOwner.getPgId());
    }
   
    @Transactional
    public Map<String, Object> updateProfilePicture(Long pgId, MultipartFile profilePicture) {
        log.info("Updating profile picture for PG ID: {}", pgId);
        
        PGManagementOwner pgManagementOwner = pgManagementRepository.findById(pgId)
            .orElseThrow(() -> new UsernameNotFoundException("PG not found"));
        
        String oldProfilePicture = pgManagementOwner.getPgProfilePicture();
        
        try {
            // Upload new profile picture
            String prefix = "pg_" + pgId;
            String newProfilePictureFileName = fileStorageService.storeFile(profilePicture, prefix);
            
            // Update entity
            pgManagementOwner.setPgProfilePicture(newProfilePictureFileName);
            pgManagementRepository.save(pgManagementOwner);
            
            // Delete old profile picture if exists
            if (oldProfilePicture != null && !oldProfilePicture.isEmpty()) {
                fileStorageService.deleteFile(oldProfilePicture);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile picture updated successfully");
            response.put("profilePictureUrl", fileStorageService.generateFileUrl(newProfilePictureFileName));
            response.put("fileName", newProfilePictureFileName);
            
            return response;
            
        } catch (Exception e) {
            log.error("Failed to update profile picture for PG ID: {}", pgId, e);
            throw new RuntimeException("Failed to update profile picture: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentUserProfile(Long pgId) {
        log.info("Getting current user profile for PG ID: {}", pgId);
        
        PGManagementOwner pgManagementOwner = pgManagementRepository.findById(pgId)
            .orElseThrow(() -> new UsernameNotFoundException("PG not found"));
        
        Map<String, Object> profile = new HashMap<>();
        
        profile.put("pgId", pgManagementOwner.getPgId());
        profile.put("pgName", pgManagementOwner.getPgName());
        profile.put("ownerName", pgManagementOwner.getOwnerName());
        profile.put("emailAddress", pgManagementOwner.getEmailAddress());
        profile.put("phoneNumber", pgManagementOwner.getPhoneNumber());
        profile.put("city", pgManagementOwner.getCity());
        profile.put("state", pgManagementOwner.getState());
        profile.put("isActive", pgManagementOwner.getIsActive());
        profile.put("emailVerified", pgManagementOwner.getEmailVerified());
        profile.put("verificationStatus", pgManagementOwner.getVerificationStatus().name());
        
        // Add profile picture URL
        if (pgManagementOwner.getPgProfilePicture() != null) {
            profile.put("profilePictureUrl", fileStorageService.generateFileUrl(pgManagementOwner.getPgProfilePicture()));
            profile.put("profilePictureFileName", pgManagementOwner.getPgProfilePicture());
        } else {
            profile.put("profilePictureUrl", null);
            profile.put("profilePictureFileName", null);
        }
        
        return profile;
    }
    
    @Transactional
    public Map<String, Object> updateProfile(Long pgId, ProfileUpdateRequest request) {
        log.info("Updating profile for PG ID: {}", pgId);
        
        // Validate and sanitize input
        request.sanitize();
        if (!request.hasRequiredFields()) {
            throw new IllegalArgumentException("All required fields must be provided");
        }
        
        PGManagementOwner pgManagementOwner = pgManagementRepository.findById(pgId)
            .orElseThrow(() -> new UsernameNotFoundException("PG not found"));
        
        // Check if phone number is being changed and if it already exists
        if (!request.getPhoneNumber().equals(pgManagementOwner.getPhoneNumber())) {
            if (pgManagementRepository.existsByPhoneNumberAndPgIdNot(request.getPhoneNumber(), pgId)) {
                throw new IllegalArgumentException("Phone number already exists");
            }
        }
        
        // Update fields
        pgManagementOwner.setOwnerName(request.getOwnerName());
        pgManagementOwner.setPgName(request.getPgName());
        pgManagementOwner.setPhoneNumber(request.getPhoneNumber());
        pgManagementOwner.setCity(request.getCity());
        pgManagementOwner.setState(request.getState());
        
        // Update optional fields if provided
        if (request.getCountry() != null && !request.getCountry().trim().isEmpty()) {
            pgManagementOwner.setCountry(request.getCountry());
        }
        if (request.getPincode() != null && !request.getPincode().trim().isEmpty()) {
            pgManagementOwner.setPincode(request.getPincode());
        }
        if (request.getLatitude() != null) {
            pgManagementOwner.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        }
        if (request.getLongitude() != null) {
            pgManagementOwner.setLongitude(BigDecimal.valueOf(request.getLongitude()));
        }
        
        pgManagementOwner = pgManagementRepository.save(pgManagementOwner);
        
        log.info("Profile updated successfully for PG ID: {}", pgId);
        
        return getCurrentUserProfile(pgManagementOwner.getPgId());
    }

   
    
    @Transactional
    public void changePassword(Long pgId, ChangePasswordRequest request) {
        log.info("Changing password for PG ID: {}", pgId);
        
        // Validate password requirements
        if (!request.isNewPasswordMatching()) {
            throw new IllegalArgumentException("New passwords do not match");
        }
        
        if (!request.isDifferentPassword()) {
            throw new IllegalArgumentException("New password must be different from current password");
        }
        
        PGManagementOwner pgManagementOwner = pgManagementRepository.findById(pgId)
            .orElseThrow(() -> new UsernameNotFoundException("PG not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), pgManagementOwner.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Update password
        String newHashedPassword = passwordEncoder.encode(request.getNewPassword());
        pgManagementOwner.setPasswordHash(newHashedPassword);
        
        // Reset login attempts and unlock account if locked
        pgManagementOwner.resetLoginAttempts();
        
        pgManagementRepository.save(pgManagementOwner);
        
        log.info("Password changed successfully for PG ID: {}", pgId);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getProfileStats(Long pgId) {
        log.info("Getting profile statistics for PG ID: {}", pgId);
        
        PGManagementOwner pgManagementOwner = pgManagementRepository.findById(pgId)
            .orElseThrow(() -> new UsernameNotFoundException("PG not found"));
        
        Map<String, Object> stats = new HashMap<>();
        
        // Profile completion percentage
        int completionPercentage = calculateProfileCompletion(pgManagementOwner);
        stats.put("profileCompletion", completionPercentage);
        
        // Account status
        stats.put("accountStatus", pgManagementOwner.getIsActive() ? "Active" : "Inactive");
        stats.put("emailVerified", pgManagementOwner.getEmailVerified());
        stats.put("phoneVerified", pgManagementOwner.getPhoneVerified());
        stats.put("verificationStatus", pgManagementOwner.getVerificationStatus().name());
        
        // Account age
        stats.put("accountCreated", pgManagementOwner.getCreatedAt());
        stats.put("lastUpdated", pgManagementOwner.getUpdatedAt());
        stats.put("lastLogin", pgManagementOwner.getLastLogin());
        
        // Security info
        stats.put("loginAttempts", pgManagementOwner.getLoginAttempts());
        stats.put("isAccountLocked", pgManagementOwner.isAccountLocked());
        
        return stats;
    }
    
    @Transactional
    public void deactivateAccount(Long pgId, String reason) {
        log.info("Deactivating account for PG ID: {}, reason: {}", pgId, reason);
        
        PGManagementOwner pgManagementOwner = pgManagementRepository.findById(pgId)
            .orElseThrow(() -> new UsernameNotFoundException("PG not found"));
        
        // Deactivate account
        pgManagementOwner.setIsActive(false);
        
        // You might want to add a deactivation reason field to your entity in the future
        // For now, we'll just log it
        
        pgManagementRepository.save(pgManagementOwner);
        
        log.info("Account deactivated successfully for PG ID: {}", pgId);
    }

    
    private int calculateProfileCompletion(PGManagementOwner pg) {
        int totalFields = 10; // Adjust based on important fields
        int completedFields = 0;
        
        if (pg.getPgName() != null && !pg.getPgName().trim().isEmpty()) completedFields++;
        if (pg.getOwnerName() != null && !pg.getOwnerName().trim().isEmpty()) completedFields++;
        if (pg.getEmailAddress() != null && !pg.getEmailAddress().trim().isEmpty()) completedFields++;
        if (pg.getPhoneNumber() != null && !pg.getPhoneNumber().trim().isEmpty()) completedFields++;
        if (pg.getCity() != null && !pg.getCity().trim().isEmpty()) completedFields++;
        if (pg.getState() != null && !pg.getState().trim().isEmpty()) completedFields++;
        if (pg.getCountry() != null && !pg.getCountry().trim().isEmpty()) completedFields++;
        if (pg.getPincode() != null && !pg.getPincode().trim().isEmpty()) completedFields++;
        if (pg.getPgProfilePicture() != null && !pg.getPgProfilePicture().trim().isEmpty()) completedFields++;
        if (pg.getEmailVerified() != null && pg.getEmailVerified()) completedFields++;
        
        return (int) ((double) completedFields / totalFields * 100);
    }
   

   

    
}