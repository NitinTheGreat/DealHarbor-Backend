package com.dealharbor.dealharbor_backend.entities;

import com.dealharbor.dealharbor_backend.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean locked;

    // Profile information
    @Column(length = 500)
    private String profilePhotoUrl;
    
    @Column(length = 500)
    private String bio;
    
    @Column(length = 20)
    private String phoneNumber;

    // OAuth fields
    @Column(length = 100)
    private String googleId;
    
    @Column(length = 100)
    private String githubId;
    
    @Column(length = 20, nullable = false)
    @Builder.Default
    private String provider = "LOCAL";

    // Security fields
    @Column(nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;
    
    private Instant lockedUntil;
    private Instant lastLoginAt;
    
    @Column(length = 45)
    private String lastLoginIp;
    
    // Account verification
    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean twoFactorEnabled = false;
    
    // Timestamps
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant updatedAt;
    
    // Account status
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
    
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (profilePhotoUrl == null) {
            profilePhotoUrl = "/api/images/default-avatar.png";
        }
        if (provider == null) {
            provider = "LOCAL";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
