package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.entities.*;
import com.dealharbor.dealharbor_backend.enums.UserRole;
import com.dealharbor.dealharbor_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    // Add these as you implement them:
    // private final EmailService emailService;
    // private final JwtTokenProvider jwtTokenProvider;

    public void register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) throw new RuntimeException("Email already exists");
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

        // Generate OTP, store & send (stub, implement EmailService next)
        String otp = generateOtp();
        OtpToken token = OtpToken.builder()
                .email(req.getEmail())
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(15*60))
                .build();
        otpTokenRepository.save(token);
        // emailService.sendOtpEmail(req.getEmail(), otp); // Implement next
        emailService.sendOtpEmail(req.getEmail(), otp);

        System.out.println("OTP (for dev only): " + otp);
    }

    public void verifyOtp(OtpVerifyRequest req) {
        OtpToken token = otpTokenRepository.findByEmailAndOtp(req.getEmail(), req.getOtp())
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));
        if (token.getExpiresAt().isBefore(Instant.now())) throw new RuntimeException("OTP expired");
        User user = userRepository.findByEmail(req.getEmail()).orElseThrow();
        user.setEnabled(true);
        userRepository.save(user);
        otpTokenRepository.deleteByEmail(req.getEmail());
    }

    // Stubs for now
    public LoginResponse login(LoginRequest req) { throw new UnsupportedOperationException("Not implemented yet"); }
    public String refreshToken(String refreshToken) { throw new UnsupportedOperationException("Not implemented yet"); }
    public void forgotPassword(ForgotPasswordRequest req) { throw new UnsupportedOperationException("Not implemented yet"); }
    public void resetPassword(ResetPasswordRequest req) { throw new UnsupportedOperationException("Not implemented yet"); }

    private String generateOtp() {
        // Simple 6-digit numeric OTP
        int otp = 100_000 + (int)(Math.random() * 900_000);
        return String.valueOf(otp);
    }
}
