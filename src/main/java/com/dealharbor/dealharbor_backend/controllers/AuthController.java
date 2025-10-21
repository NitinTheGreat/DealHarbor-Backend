package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
// Allow known frontend origins and credentials so session cookies can be set/read
@CrossOrigin(
    origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
    allowCredentials = "true"
)
public class AuthController {
    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;

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
    public ResponseEntity<?> login(@RequestBody LoginRequest req,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        Authentication authentication = authService.login(req, request);

        // Create a fresh SecurityContext and persist it via the configured repository
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // Ensure an HTTP session exists before changing session id (required when app is stateless by default)
        // This will back the session by Redis due to @EnableRedisHttpSession
        request.getSession(true);

        // Regenerate session id to prevent fixation and save context to session/Redis
        request.changeSessionId();
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.ok("Logged in successfully.");
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

    @GetMapping("/session-info")
    public ResponseEntity<Map<String, Object>> getSessionInfo(HttpSession session, Authentication authentication) {
        Map<String, Object> data = new HashMap<>();
        long now = System.currentTimeMillis();
        long created = session.getCreationTime();
        long last = session.getLastAccessedTime();
        int maxInactive = session.getMaxInactiveInterval(); // seconds
        long expiresAt = last + (maxInactive * 1000L);
        long remaining = Math.max(0L, (expiresAt - now) / 1000L);

        data.put("sessionId", session.getId());
        data.put("creationTime", created);
        data.put("lastAccessedTime", last);
        data.put("maxInactiveIntervalSeconds", maxInactive);
        data.put("now", now);
        data.put("expiresAt", expiresAt);
        data.put("secondsRemaining", remaining);
        if (authentication != null) {
            data.put("principalName", authentication.getName());
            data.put("authenticated", authentication.isAuthenticated());
        } else {
            data.put("principalName", null);
            data.put("authenticated", false);
        }
        return ResponseEntity.ok(data);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session,
                    HttpServletRequest request,
                    HttpServletResponse response) {
    // Invalidate server-side session (removes it from Redis via Spring Session)
    session.invalidate();

    // Clear SecurityContext just in case
    SecurityContextHolder.clearContext();

    // Instruct client to delete session cookies (Spring Session uses "SESSION").
    boolean secure = request.isSecure();
    ResponseCookie sessionCookie = ResponseCookie.from("SESSION", "")
        .httpOnly(true)
        .secure(secure)
        .path("/")
        .maxAge(0)
        .sameSite("Lax")
        .build();
    // Also clear JSESSIONID if present in some containers/tools
    ResponseCookie jsessionCookie = ResponseCookie.from("JSESSIONID", "")
        .httpOnly(true)
        .secure(secure)
        .path("/")
        .maxAge(0)
        .sameSite("Lax")
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
        .header(HttpHeaders.SET_COOKIE, jsessionCookie.toString())
        .body("Logged out successfully.");
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
