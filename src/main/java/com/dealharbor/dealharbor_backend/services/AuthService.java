package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.entities.*;
import com.dealharbor.dealharbor_backend.enums.UserRole;
import com.dealharbor.dealharbor_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final EmailService emailService;
    private final SecurityService securityService;
    private final StudentVerificationService studentVerificationService;
    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        String hashed = passwordEncoder.encode(req.getPassword());
        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(hashed)
                .name(req.getName())
                .role(UserRole.USER)
                .enabled(false)
                .locked(false)
                .emailVerified(false)
                .twoFactorEnabled(false)
                .failedLoginAttempts(0)
                .deleted(false)
                .provider("LOCAL")
                .profilePhotoUrl("/api/images/default-avatar.png")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        user = userRepository.save(user);
        
        // Auto-verify student if they registered with student email
        studentVerificationService.autoVerifyStudentDuringRegistration(user);

        generateAndSendOtp(req.getEmail());
    }

    @Transactional
    public void resendOtp(ResendOtpRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isDeleted()) {
            throw new RuntimeException("Account has been deleted");
        }
        
        if (user.isEnabled()) {
            throw new RuntimeException("User is already verified");
        }

        generateAndSendOtp(req.getEmail());
    }

    @Transactional
    public void verifyOtp(OtpVerifyRequest req) {
        OtpToken token = otpTokenRepository.findByEmailAndOtp(req.getEmail(), req.getOtp())
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));
        
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("OTP expired");
        }
        
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isDeleted()) {
            throw new RuntimeException("Account has been deleted");
        }
        
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        otpTokenRepository.deleteByEmail(req.getEmail());
        
        // Send welcome email if student was auto-verified
        if (user.isVerifiedStudent()) {
            emailService.sendStudentVerificationSuccess(user.getEmail(), user.getName());
        }
    }

    public Authentication login(LoginRequest req, jakarta.servlet.http.HttpServletRequest request) {
        User user = userRepository.findByEmail(req.getEmail()).orElse(null);

        if (user == null || user.isDeleted()) {
            // Record failed attempt with context
            recordAttemptSafe(req.getEmail(), request, false);
            throw new RuntimeException("REDIRECT_TO_SIGNUP");
        }

        if (!user.isEnabled()) {
            recordAttemptSafe(req.getEmail(), request, false);
            throw new RuntimeException("Account not verified. Please verify your email first.");
        }

        if (user.isLocked() || securityService.isAccountLocked(req.getEmail())) {
            recordAttemptSafe(req.getEmail(), request, false);
            throw new RuntimeException("Account is temporarily locked due to multiple failed login attempts");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );

            // Record successful login attempt and event
            recordAttemptSafe(req.getEmail(), request, true);
            securityService.recordSecurityEvent(
                    user.getId(),
                    "LOGIN",
                    request != null ? request.getRemoteAddr() : "N/A",
                    request != null ? request.getHeader("User-Agent") : "N/A",
                    "Login via EMAIL_PASSWORD"
            );

            return authentication;
        } catch (Exception e) {
            recordAttemptSafe(req.getEmail(), request, false);
            throw new RuntimeException("Invalid credentials");
        }
    }

    private void recordAttemptSafe(String email, jakarta.servlet.http.HttpServletRequest request, boolean successful) {
        try {
            String ip = request != null ? request.getHeader("X-Forwarded-For") : null;
            if (ip != null && !ip.isEmpty()) {
                ip = ip.split(",")[0].trim();
            } else {
                ip = request != null ? request.getRemoteAddr() : "N/A";
            }
            String ua = request != null ? request.getHeader("User-Agent") : "N/A";
            securityService.recordLoginAttempt(email, ip, ua, successful);
        } catch (Exception ignored) {
        }
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        User user = userRepository.findByEmail(req.getEmail()).orElse(null);
        
        if (user == null || user.isDeleted() || !user.isEnabled()) {
            throw new RuntimeException("Email not found or not verified");
        }

        String otp = generateOtp();
        otpTokenRepository.deleteByEmail(req.getEmail());
        
        OtpToken token = OtpToken.builder()
                .email(req.getEmail())
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(15 * 60))
                .build();
        otpTokenRepository.save(token);
        
        emailService.sendForgotPasswordOtp(req.getEmail(), otp);
        System.out.println("Password Reset OTP (for dev only): " + otp);
        
        securityService.recordSecurityEvent(user.getId(), "PASSWORD_RESET_REQUESTED", "N/A", "N/A", "Password reset requested");
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        OtpToken token = otpTokenRepository.findByEmailAndOtp(req.getEmail(), req.getOtp())
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));
        
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("OTP expired");
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isDeleted()) {
            throw new RuntimeException("Account has been deleted");
        }

        // Check if new password is same as old password
        if (passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("New password cannot be the same as your current password");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        
        otpTokenRepository.deleteByEmail(req.getEmail());
        
        securityService.recordSecurityEvent(user.getId(), "PASSWORD_RESET", "N/A", "N/A", "Password reset via OTP");
    }

    public CheckEmailResponse checkEmail(CheckEmailRequest req) {
        User user = userRepository.findByEmail(req.getEmail()).orElse(null);
        if (user == null || user.isDeleted()) {
            return new CheckEmailResponse(false, false);
        }
        return new CheckEmailResponse(true, user.isEnabled());
    }

    public EmailResetEligibilityResponse checkEmailForReset(CheckEmailRequest req) {
        User user = userRepository.findByEmail(req.getEmail()).orElse(null);
        
        if (user == null || user.isDeleted()) {
            return new EmailResetEligibilityResponse(false, false, false, "Email not found");
        }
        
        if (!user.isEnabled()) {
            return new EmailResetEligibilityResponse(true, false, false, "Email not verified");
        }
        
        return new EmailResetEligibilityResponse(true, true, true, "Email eligible for password reset");
    }

    // Protected methods (authentication required)

    public UserProfileResponse getCurrentUser(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getBio(),
                user.getPhoneNumber(),
                user.getProfilePhotoUrl(),
                user.getRole(),
                user.isEnabled(),
                user.isLocked(),
                user.getProvider(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                // Seller Badge System Fields
                user.getSellerBadge(),
                user.getSellerRating(),
                user.getBuyerRating(),
                user.getTotalSales(),
                user.getTotalPurchases(),
                user.getTotalListings(),
                user.getActiveListings(),
                user.getTotalRevenue(),
                user.getResponseRate(),
                user.getPositiveReviews(),
                user.getNegativeReviews(),
                user.getFirstSaleAt(),
                // University fields
                user.getUniversityId(),
                user.getGraduationYear(),
                user.getDepartment(),
                user.isVerifiedStudent(),
                // Computed fields
                user.getOverallRating(),
                user.getSellerSuccessRate()
        );
    }

    @Transactional
    public void logoutAll(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        userSessionRepository.deactivateAllUserSessions(user.getId());
        
        securityService.recordSecurityEvent(user.getId(), "LOGOUT_ALL", "N/A", "N/A", "Logged out from all devices");
    }

    @Transactional
    public void changePassword(ChangePasswordRequest req, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        
        userSessionRepository.deactivateAllUserSessions(user.getId());
        
        securityService.recordSecurityEvent(user.getId(), "PASSWORD_CHANGE", "N/A", "N/A", "Password changed successfully");
    }

    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest req, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        user.setName(req.getName());
        user.setBio(req.getBio());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);
        
        securityService.recordSecurityEvent(user.getId(), "PROFILE_UPDATE", "N/A", "N/A", "Profile updated");
        
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getBio(),
                user.getPhoneNumber(),
                user.getProfilePhotoUrl(),
                user.getRole(),
                user.isEnabled(),
                user.isLocked(),
                user.getProvider(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                // Seller Badge System Fields
                user.getSellerBadge(),
                user.getSellerRating(),
                user.getBuyerRating(),
                user.getTotalSales(),
                user.getTotalPurchases(),
                user.getTotalListings(),
                user.getActiveListings(),
                user.getTotalRevenue(),
                user.getResponseRate(),
                user.getPositiveReviews(),
                user.getNegativeReviews(),
                user.getFirstSaleAt(),
                // University fields
                user.getUniversityId(),
                user.getGraduationYear(),
                user.getDepartment(),
                user.isVerifiedStudent(),
                // Computed fields
                user.getOverallRating(),
                user.getSellerSuccessRate()
        );
    }

    @Transactional
    public UserProfileResponse updateProfilePhoto(String photoUrl, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        user.setProfilePhotoUrl(photoUrl);
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);
        
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getBio(),
                user.getPhoneNumber(),
                user.getProfilePhotoUrl(),
                user.getRole(),
                user.isEnabled(),
                user.isLocked(),
                user.getProvider(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                // Seller Badge System Fields
                user.getSellerBadge(),
                user.getSellerRating(),
                user.getBuyerRating(),
                user.getTotalSales(),
                user.getTotalPurchases(),
                user.getTotalListings(),
                user.getActiveListings(),
                user.getTotalRevenue(),
                user.getResponseRate(),
                user.getPositiveReviews(),
                user.getNegativeReviews(),
                user.getFirstSaleAt(),
                // University fields
                user.getUniversityId(),
                user.getGraduationYear(),
                user.getDepartment(),
                user.isVerifiedStudent(),
                // Computed fields
                user.getOverallRating(),
                user.getSellerSuccessRate()
        );
    }

    public List<UserSessionResponse> getActiveSessions(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        List<UserSession> sessions = securityService.getActiveSessions(user.getId());
        
        return sessions.stream()
                .map(session -> new UserSessionResponse(
                        session.getId(),
                        session.getIpAddress(),
                        session.getDeviceInfo(),
                        session.getCreatedAt(),
                        session.getLastUsedAt(),
                        false
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void terminateSession(String sessionId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        securityService.terminateSession(sessionId);
        securityService.recordSecurityEvent(user.getId(), "SESSION_TERMINATED", "N/A", "N/A", "Session terminated: " + sessionId);
    }

    public List<SecurityEventResponse> getSecurityEvents(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        List<SecurityEvent> events = securityService.getRecentSecurityEvents(user.getId());
        
        return events.stream()
                .map(event -> new SecurityEventResponse(
                        event.getEventType(),
                        event.getIpAddress(),
                        event.getDescription(),
                        event.getTimestamp()
                ))
                .collect(Collectors.toList());
    }

    public AccountStatsResponse getAccountStats(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        List<UserSession> activeSessions = securityService.getActiveSessions(user.getId());
        
        return new AccountStatsResponse(
                activeSessions.size(),
                user.getLastLoginAt(),
                user.getLastLoginIp(),
                user.getFailedLoginAttempts(),
                user.isTwoFactorEnabled(),
                user.getCreatedAt()
        );
    }

    @Transactional
    public void changeEmail(ChangeEmailRequest req, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Password is incorrect");
        }
        
        if (userRepository.existsByEmail(req.getNewEmail())) {
            throw new RuntimeException("Email already exists");
        }

        String otp = generateOtp();
        otpTokenRepository.deleteByEmail(req.getNewEmail());
        
        OtpToken token = OtpToken.builder()
                .email(req.getNewEmail())
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(15 * 60))
                .build();
        otpTokenRepository.save(token);
        
        emailService.sendEmailChangeOtp(req.getNewEmail(), otp);
        System.out.println("Email Change OTP (for dev only): " + otp);
        
        securityService.recordSecurityEvent(user.getId(), "EMAIL_CHANGE_REQUESTED", "N/A", "N/A", "Email change requested to: " + req.getNewEmail());
    }

    @Transactional
    public void verifyEmailChange(OtpVerifyRequest req, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        
        OtpToken token = otpTokenRepository.findByEmailAndOtp(req.getEmail(), req.getOtp())
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));
        
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("OTP expired");
        }
        
        String oldEmail = user.getEmail();
        user.setEmail(req.getEmail());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        
        otpTokenRepository.deleteByEmail(req.getEmail());
        
        userSessionRepository.deactivateAllUserSessions(user.getId());
        
        securityService.recordSecurityEvent(user.getId(), "EMAIL_CHANGED", "N/A", "N/A", "Email changed from " + oldEmail + " to " + req.getEmail());
    }

    @Transactional
    public void deleteAccount(DeleteAccountRequest req, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Password is incorrect");
        }
        
        user.setDeleted(true);
        user.setDeletedAt(Instant.now());
        user.setEnabled(false);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        
        userSessionRepository.deactivateAllUserSessions(user.getId());
        otpTokenRepository.deleteByEmail(user.getEmail());
        
        securityService.recordSecurityEvent(user.getId(), "ACCOUNT_DELETED", "N/A", "N/A", 
                "Account deleted. Reason: " + (req.getReason() != null ? req.getReason() : "Not specified"));
        
        emailService.sendAccountDeletionConfirmation(user.getEmail(), user.getName());
    }

    // Helper methods

    private User getUserFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void generateAndSendOtp(String email) {
        String otp = generateOtp();
        otpTokenRepository.deleteByEmail(email);
        
        OtpToken token = OtpToken.builder()
                .email(email)
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(15 * 60))
                .build();
        otpTokenRepository.save(token);
        
        emailService.sendOtpEmail(email, otp);
        System.out.println("OTP (for dev only): " + otp);
    }

    private String generateOtp() {
        int otp = 100_000 + (int)(Math.random() * 900_000);
        return String.valueOf(otp);
    }
}
