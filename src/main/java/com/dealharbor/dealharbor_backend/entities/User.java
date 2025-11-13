package com.dealharbor.dealharbor_backend.entities;

import com.dealharbor.dealharbor_backend.enums.UserRole;
import com.dealharbor.dealharbor_backend.enums.SellerBadge;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
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

    // University information (Updated for simplified verification)
    @Column(length = 255, unique = true)
    private String universityEmail; // VIT student email
    
    @Column(length = 100)
    private String universityId; // Student/Staff ID
    
    private Integer graduationYear;
    
    @Column(length = 100)
    private String department;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isVerifiedStudent = false;

    // OAuth fields
    @Column(length = 100)
    private String googleId;
    
    @Column(length = 100)
    private String githubId;
    
    @Column(length = 20, nullable = false)
    @Builder.Default
    private String provider = "LOCAL";

    // Seller Performance & Badge System
    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal sellerRating = BigDecimal.valueOf(0.00);
    
    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal buyerRating = BigDecimal.valueOf(0.00);
    
    @Column(nullable = false)
    @Builder.Default
    private Integer totalSales = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer totalPurchases = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer totalListings = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer activeListings = 0;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SellerBadge sellerBadge = SellerBadge.NEW_SELLER;
    
    private Instant firstSaleAt;
    
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.valueOf(0.00);
    
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal responseRate = BigDecimal.valueOf(0.00);
    
    @Column(nullable = false)
    @Builder.Default
    private Integer positiveReviews = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer negativeReviews = 0;

    // Account status
    @Column(nullable = false)
    @Builder.Default
    private boolean isBanned = false;
    
    private Instant bannedUntil;
    
    @Column(length = 500)
    private String banReason;

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
        if (sellerBadge == null) {
            sellerBadge = SellerBadge.NEW_SELLER;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Helper method to calculate seller success rate
    public BigDecimal getSellerSuccessRate() {
        if (totalListings == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(totalSales.doubleValue() / totalListings.doubleValue() * 100)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    // Helper method to get overall rating (combination of seller and buyer)
    public BigDecimal getOverallRating() {
        if (sellerRating.equals(BigDecimal.ZERO) && buyerRating.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        if (sellerRating.equals(BigDecimal.ZERO)) return buyerRating;
        if (buyerRating.equals(BigDecimal.ZERO)) return sellerRating;
        return sellerRating.add(buyerRating).divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
    }
}
