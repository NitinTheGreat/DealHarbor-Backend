package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.SellerProfileResponse;
import com.dealharbor.dealharbor_backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(
    origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
    allowCredentials = "true"
)
public class UserController {
    
    private final UserService userService;
    
    /**
     * Get seller/user profile by ID
     * Public endpoint - returns different data based on authentication
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<SellerProfileResponse> getUserProfile(
            @PathVariable String userId,
            Authentication authentication) {
        
        // If user is authenticated, return full profile (with phone number)
        // If not authenticated, return public profile only
        SellerProfileResponse profile;
        
        if (authentication != null && authentication.isAuthenticated()) {
            profile = userService.getSellerProfile(userId);
        } else {
            profile = userService.getPublicSellerProfile(userId);
        }
        
        return ResponseEntity.ok(profile);
    }
    
    /**
     * Get current authenticated user's profile
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<SellerProfileResponse> getMyProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String userId = authentication.getName(); // Username is the user ID
        SellerProfileResponse profile = userService.getSellerProfile(userId);
        
        return ResponseEntity.ok(profile);
    }
}
