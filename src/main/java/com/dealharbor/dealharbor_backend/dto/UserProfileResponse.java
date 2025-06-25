package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.UserRole;
import com.dealharbor.dealharbor_backend.enums.SellerBadge;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private String id;
    private String email;
    private String name;
    private String bio;
    private String phoneNumber;
    private String profilePhotoUrl;
    private UserRole role;
    private boolean enabled;
    private boolean locked;
    private String provider;
    private Instant createdAt;
    private Instant lastLoginAt;
    
    // Seller Badge System Fields
    private SellerBadge sellerBadge;
    private BigDecimal sellerRating;
    private BigDecimal buyerRating;
    private Integer totalSales;
    private Integer totalPurchases;
    private Integer totalListings;
    private Integer activeListings;
    private BigDecimal totalRevenue;
    private BigDecimal responseRate;
    private Integer positiveReviews;
    private Integer negativeReviews;
    private Instant firstSaleAt;
    
    // University fields
    private String universityId;
    private Integer graduationYear;
    private String department;
    private boolean isVerifiedStudent;
    
    // Computed fields
    private BigDecimal overallRating;
    private BigDecimal sellerSuccessRate;
}
