package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.entities.*;
import com.dealharbor.dealharbor_backend.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityService {
    private final LoginAttemptRepository loginAttemptRepository;
    private final UserSessionRepository userSessionRepository;
    private final SecurityEventRepository securityEventRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    @Transactional
    public void recordLoginAttempt(String email, String ipAddress, String userAgent, boolean successful) {
        LoginAttempt attempt = LoginAttempt.builder()
                .email(email)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .successful(successful)
                .attemptTime(Instant.now())
                .build();
        loginAttemptRepository.save(attempt);

        if (!successful) {
            handleFailedLogin(email);
        } else {
            handleSuccessfulLogin(email, ipAddress);
        }
    }

    @Transactional
    public void recordSecurityEvent(String userId, String eventType, String ipAddress, String userAgent, String description) {
        SecurityEvent event = SecurityEvent.builder()
                .userId(userId)
                .eventType(eventType)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .description(description)
                .timestamp(Instant.now())
                .build();
        securityEventRepository.save(event);
    }

    @Transactional
    public UserSession createUserSession(String userId, String refreshToken, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String deviceInfo = extractDeviceInfo(userAgent);

        UserSession session = UserSession.builder()
                .userId(userId)
                .refreshToken(refreshToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceInfo(deviceInfo)
                .createdAt(Instant.now())
                .lastUsedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60)) // 7 days
                .active(true)
                .build();

        return userSessionRepository.save(session);
    }

    public boolean isAccountLocked(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            return true;
        }

        // Check recent failed attempts
        Instant since = Instant.now().minusSeconds(30 * 60); // 30 minutes
        int failedAttempts = loginAttemptRepository.countFailedAttemptsSince(email, since);
        
        return failedAttempts >= MAX_FAILED_ATTEMPTS;
    }

    @Transactional
    public void unlockAccount(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setLocked(false);
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
    }

    public List<UserSession> getActiveSessions(String userId) {
        return userSessionRepository.findByUserIdAndActiveTrue(userId);
    }

    public List<SecurityEvent> getRecentSecurityEvents(String userId) {
        return securityEventRepository.findTop10ByUserIdOrderByTimestampDesc(userId);
    }

    @Transactional
    public void terminateSession(String sessionId) {
        userSessionRepository.findById(sessionId).ifPresent(session -> {
            session.setActive(false);
            userSessionRepository.save(session);
        });
    }

    @Transactional
    public void terminateAllSessions(String userId) {
        userSessionRepository.deactivateAllUserSessions(userId);
    }

    private void handleFailedLogin(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return;

        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setLocked(true);
            user.setLockedUntil(Instant.now().plusSeconds(LOCKOUT_DURATION_MINUTES * 60));
            
            // Send security alert email
            emailService.sendSecurityAlert(user.getEmail(), "Account Locked", 
                "Your account has been temporarily locked due to multiple failed login attempts.");
        }

        userRepository.save(user);
    }

    private void handleSuccessfulLogin(String email, String ipAddress) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return;

        // Reset failed attempts
        user.setFailedLoginAttempts(0);
        user.setLocked(false);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        user.setLastLoginIp(ipAddress);

        userRepository.save(user);
    }

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

    private String extractDeviceInfo(String userAgent) {
        if (userAgent == null) return "Unknown Device";
        
        if (userAgent.contains("Mobile")) return "Mobile Device";
        if (userAgent.contains("Tablet")) return "Tablet";
        if (userAgent.contains("Windows")) return "Windows PC";
        if (userAgent.contains("Mac")) return "Mac";
        if (userAgent.contains("Linux")) return "Linux";
        
        return "Unknown Device";
    }
}
