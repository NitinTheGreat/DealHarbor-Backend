package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.StudentVerificationRequest;
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

    @PostMapping("/verify")
    public ResponseEntity<?> verifyStudent(
            @RequestBody StudentVerificationRequest request,
            Authentication authentication) {
        studentVerificationService.submitVerification(request, authentication);
        return ResponseEntity.ok("Student verification successful! You are now a verified VIT student.");
    }
}
