package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.entities.*;
import com.dealharbor.dealharbor_backend.enums.UserRole;
import com.dealharbor.dealharbor_backend.repositories.*;
import com.dealharbor.dealharbor_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
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
                .createdAt(Instant.now())
                .build();
        userRepository.save(user);

        // Generate and send OTP
        generateAndSendOtp(req.getEmail());
    }

    @Transactional
    public void resendOtp(ResendOtpRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isEnabled()) {
            throw new RuntimeException("User is already verified");
        }

        // Generate and send new OTP
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
        
        user.setEnabled(true);
        userRepository.save(user);
        otpTokenRepository.deleteByEmail(req.getEmail());
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified. Please verify your email first.");
        }
        
        if (user.isLocked()) {
            throw new RuntimeException("Account is locked");
        }

        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        // Generate tokens
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        if (token.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = createRefreshToken(user);

        // Delete old refresh token
        refreshTokenRepository.delete(token);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified");
        }

        // Generate and send OTP for password reset
        String otp = generateOtp();
        
        // Delete existing OTP tokens for this email
        otpTokenRepository.deleteByEmail(req.getEmail());
        
        OtpToken token = OtpToken.builder()
                .email(req.getEmail())
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(15 * 60)) // 15 minutes
                .build();
        otpTokenRepository.save(token);
        
        emailService.sendForgotPasswordOtp(req.getEmail(), otp);
        System.out.println("Password Reset OTP (for dev only): " + otp);
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

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        
        // Delete OTP token and all refresh tokens for this user
        otpTokenRepository.deleteByEmail(req.getEmail());
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    private void generateAndSendOtp(String email) {
        String otp = generateOtp();
        
        // Delete existing OTP tokens for this email
        otpTokenRepository.deleteByEmail(email);
        
        OtpToken token = OtpToken.builder()
                .email(email)
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(15 * 60)) // 15 minutes
                .build();
        otpTokenRepository.save(token);
        
        emailService.sendOtpEmail(email, otp);
        System.out.println("OTP (for dev only): " + otp);
    }

    private String createRefreshToken(User user) {
        // Delete existing refresh tokens for this user
        refreshTokenRepository.deleteByUserId(user.getId());
        
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .userId(user.getId())
                .expiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60)) // 7 days
                .build();
        refreshTokenRepository.save(refreshToken);
        
        return tokenValue;
    }

    private String generateOtp() {
        int otp = 100_000 + (int)(Math.random() * 900_000);
        return String.valueOf(otp);
    }
}
