package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.services.StudentVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student-verification")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentVerificationController {
    
    private final StudentVerificationService studentVerificationService;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendStudentEmailOtp(
            @RequestBody StudentEmailOtpRequest request,
            Authentication authentication) {
        studentVerificationService.sendStudentEmailOtp(request, authentication);
        return ResponseEntity.ok("OTP sent to your student email. Please check your inbox.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyStudentEmailOtp(
            @RequestBody StudentEmailOtpVerifyRequest request,
            Authentication authentication) {
        studentVerificationService.verifyStudentEmailOtp(request, authentication);
        return ResponseEntity.ok("Student verification successful! You are now a verified VIT student.");
    }
}
