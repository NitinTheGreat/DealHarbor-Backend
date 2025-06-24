package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.StudentVerificationRequest;
import com.dealharbor.dealharbor_backend.entities.User;
import com.dealharbor.dealharbor_backend.repositories.UserRepository;
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
    private final SecurityService securityService;
    
    private static final List<String> VALID_DOMAINS = Arrays.asList(
        "@vitstudent.ac.in",
        "@vit.ac.in", 
        "@vitchennai.ac.in"
    );

    @Transactional
    public void submitVerification(StudentVerificationRequest request, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        
        // Validate student email domain
        if (!isValidStudentEmail(request.getStudentEmail())) {
            throw new RuntimeException("Invalid student email. Must be from VIT domain (@vitstudent.ac.in, @vit.ac.in, or @vitchennai.ac.in)");
        }
        
        // Check if email is already used by another verified student
        if (userRepository.existsByUniversityEmailAndIsVerifiedStudentTrue(request.getStudentEmail())) {
            throw new RuntimeException("This student email is already verified by another user");
        }
        
        // Update user with student information
        user.setUniversityEmail(request.getStudentEmail());
        user.setUniversityId(request.getUniversityId());
        user.setGraduationYear(request.getGraduationYear());
        user.setDepartment(request.getDepartment());
        user.setVerifiedStudent(true); // Auto-verify if domain is valid
        user.setUpdatedAt(Instant.now());
        
        userRepository.save(user);
        
        securityService.recordSecurityEvent(user.getId(), "STUDENT_VERIFIED", 
                "N/A", "N/A", "Student automatically verified with email: " + request.getStudentEmail());
    }
    
    private boolean isValidStudentEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String lowerEmail = email.toLowerCase().trim();
        return VALID_DOMAINS.stream().anyMatch(lowerEmail::endsWith);
    }

    private User getUserFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
