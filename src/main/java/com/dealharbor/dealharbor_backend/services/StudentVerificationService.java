package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.entities.*;
import com.dealharbor.dealharbor_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentVerificationService {
    
    private final UserRepository userRepository;
    private final StudentOtpTokenRepository studentOtpTokenRepository;
    private final SecurityService securityService;
    private final EmailService emailService;
    
    private static final List<String> VALID_DOMAINS = Arrays.asList(
        "@vitstudent.ac.in",
        "@vit.ac.in", 
        "@vitchennai.ac.in"
    );

    @Transactional
    public void sendStudentEmailOtp(StudentEmailOtpRequest request, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        
        // Validate student email domain
        if (!isValidStudentEmail(request.getStudentEmail())) {
            throw new RuntimeException("Invalid student email. Must be from VIT domain (@vitstudent.ac.in, @vit.ac.in, or @vitchennai.ac.in)");
        }
        
        // Check if email is already used by another verified student
        if (userRepository.existsByUniversityEmailAndIsVerifiedStudentTrue(request.getStudentEmail())) {
            throw new RuntimeException("This student email is already verified by another user");
        }
        
        // Check if user is already verified with a different email
        if (user.isVerifiedStudent() && user.getUniversityEmail() != null && 
            !user.getUniversityEmail().equals(request.getStudentEmail())) {
            throw new RuntimeException("You are already verified with a different student email");
        }
        
        // Generate and send OTP
        String otp = generateOtp();
        
        // Delete any existing OTP tokens for this user
        studentOtpTokenRepository.deleteByUserId(user.getId());
        
        StudentOtpToken token = StudentOtpToken.builder()
                .userId(user.getId())
                .studentEmail(request.getStudentEmail())
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(15 * 60)) // 15 minutes
                .build();
        
        studentOtpTokenRepository.save(token);
        
        // Send OTP email
        emailService.sendStudentVerificationOtp(request.getStudentEmail(), otp, user.getName());
        System.out.println("Student Verification OTP (for dev only): " + otp);
        
        securityService.recordSecurityEvent(user.getId(), "STUDENT_OTP_SENT", 
                "N/A", "N/A", "Student verification OTP sent to: " + request.getStudentEmail());
    }

    @Transactional
    public void verifyStudentEmailOtp(StudentEmailOtpVerifyRequest request, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        
        // Find and validate OTP token
        StudentOtpToken token = studentOtpTokenRepository.findByUserIdAndStudentEmailAndOtp(
                user.getId(), request.getStudentEmail(), request.getOtp())
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));
        
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("OTP expired");
        }
        
        // Double-check email is not used by another verified student
        if (userRepository.existsByUniversityEmailAndIsVerifiedStudentTrue(request.getStudentEmail())) {
            throw new RuntimeException("This student email is already verified by another user");
        }
        
        // Update user with student information
        user.setUniversityEmail(request.getStudentEmail());
        user.setUniversityId(request.getUniversityId());
        user.setGraduationYear(request.getGraduationYear());
        user.setDepartment(request.getDepartment());
        user.setVerifiedStudent(true);
        user.setUpdatedAt(Instant.now());
        
        userRepository.save(user);
        
        // Clean up OTP token
        studentOtpTokenRepository.deleteByUserId(user.getId());
        
        securityService.recordSecurityEvent(user.getId(), "STUDENT_VERIFIED", 
                "N/A", "N/A", "Student verified with email: " + request.getStudentEmail());
    }
    
    /**
     * Auto-verify student during registration if they use student email
     */
    @Transactional
    public void autoVerifyStudentDuringRegistration(User user) {
        if (isValidStudentEmail(user.getEmail())) {
            // Check if this student email is already used
            if (userRepository.existsByUniversityEmailAndIsVerifiedStudentTrue(user.getEmail())) {
                throw new RuntimeException("This student email is already registered and verified");
            }
            
            user.setUniversityEmail(user.getEmail());
            user.setVerifiedStudent(true);
            userRepository.save(user);
            
            securityService.recordSecurityEvent(user.getId(), "AUTO_STUDENT_VERIFIED", 
                    "N/A", "N/A", "Student auto-verified during registration with email: " + user.getEmail());
        }
    }
    
    private boolean isValidStudentEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String lowerEmail = email.toLowerCase().trim();
        return VALID_DOMAINS.stream().anyMatch(lowerEmail::endsWith);
    }
    
    private String generateOtp() {
        int otp = 100_000 + (int)(Math.random() * 900_000);
        return String.valueOf(otp);
    }

    private User getUserFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
