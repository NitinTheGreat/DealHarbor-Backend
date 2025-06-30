package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn; // in seconds
    private UserInfo user;
    private boolean needsStudentVerification; // Add this field

    // Constructor for backward compatibility
    public LoginResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.expiresIn = 3600; // default 1 hour
        this.needsStudentVerification = false;
    }

    public LoginResponse(String accessToken, String refreshToken, long expiresIn, UserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.user = user;
        this.needsStudentVerification = false;
    }

    public LoginResponse(String accessToken, String refreshToken, long expiresIn, UserInfo user, boolean needsStudentVerification) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.user = user;
        this.needsStudentVerification = needsStudentVerification;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String role;
        private boolean isStudentVerified;
        private String profilePhotoUrl;
    }
}
