package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.services.AuthService;
import com.dealharbor.dealharbor_backend.services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;
    private final SecurityService securityService;

    // Public endpoints (no authentication required)
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.ok("Registration successful. OTP sent to email.");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody OtpVerifyRequest req) {
        authService.verifyOtp(req);
        return ResponseEntity.ok("Email verified successfully. You can now log in.");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody ResendOtpRequest req) {
        authService.resendOtp(req);
        return ResponseEntity.ok("New OTP sent to email.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return ResponseEntity.ok("Password reset OTP sent to email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok("Password reset successfully.");
    }

    @PostMapping("/check-email")
    public ResponseEntity<CheckEmailResponse> checkEmail(@RequestBody CheckEmailRequest req) {
        return ResponseEntity.ok(authService.checkEmail(req));
    }

    @PostMapping("/check-email-for-reset")
    public ResponseEntity<EmailResetEligibilityResponse> checkEmailForReset(@RequestBody CheckEmailRequest req) {
        return ResponseEntity.ok(authService.checkEmailForReset(req));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("Auth endpoints are working!");
    }

    // Protected endpoints (authentication required)

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUser(authentication));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok("Logged out successfully.");
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(Authentication authentication) {
        authService.logoutAll(authentication);
        return ResponseEntity.ok("Logged out from all devices successfully.");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req, Authentication authentication) {
        authService.changePassword(req, authentication);
        return ResponseEntity.ok("Password changed successfully.");
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestBody UpdateProfileRequest req, Authentication authentication) {
        return ResponseEntity.ok(authService.updateProfile(req, authentication));
    }

    @PutMapping("/profile-photo")
    public ResponseEntity<UserProfileResponse> updateProfilePhoto(@RequestBody String photoUrl, Authentication authentication) {
        return ResponseEntity.ok(authService.updateProfilePhoto(photoUrl, authentication));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<UserSessionResponse>> getActiveSessions(Authentication authentication) {
        return ResponseEntity.ok(authService.getActiveSessions(authentication));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> terminateSession(@PathVariable String sessionId, Authentication authentication) {
        authService.terminateSession(sessionId, authentication);
        return ResponseEntity.ok("Session terminated successfully.");
    }

    @GetMapping("/security-events")
    public ResponseEntity<List<SecurityEventResponse>> getSecurityEvents(Authentication authentication) {
        return ResponseEntity.ok(authService.getSecurityEvents(authentication));
    }

    @GetMapping("/account-stats")
    public ResponseEntity<AccountStatsResponse> getAccountStats(Authentication authentication) {
        return ResponseEntity.ok(authService.getAccountStats(authentication));
    }

    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(@RequestBody ChangeEmailRequest req, Authentication authentication) {
        authService.changeEmail(req, authentication);
        return ResponseEntity.ok("Email change OTP sent to new email address.");
    }

    @PostMapping("/verify-email-change")
    public ResponseEntity<?> verifyEmailChange(@RequestBody OtpVerifyRequest req, Authentication authentication) {
        authService.verifyEmailChange(req, authentication);
        return ResponseEntity.ok("Email changed successfully.");
    }

    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(@RequestBody DeleteAccountRequest req, Authentication authentication) {
        authService.deleteAccount(req, authentication);
        return ResponseEntity.ok("Account deleted successfully.");
    }
}
