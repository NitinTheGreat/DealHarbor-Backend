package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.SellerBadge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileResponse {
    private String id;
    private String name;
    private String profilePhotoUrl;
    private String bio;
    private String phoneNumber;
    
    // University info
    private boolean isVerifiedStudent;
    private String department;
    private Integer graduationYear;
    
    // Seller metrics
    private BigDecimal sellerRating;
    private BigDecimal buyerRating;
    private BigDecimal overallRating;
    private Integer totalSales;
    private Integer totalPurchases;
    private Integer totalListings;
    private Integer activeListings;
    private SellerBadge sellerBadge;
    private Instant firstSaleAt;
    private BigDecimal totalRevenue;
    private BigDecimal responseRate;
    private Integer positiveReviews;
    private Integer negativeReviews;
    private BigDecimal successRate;
    
    // Account info
    private Instant memberSince;
    private Instant lastLoginAt;
    
    // Social proof
    private Integer totalReviews;
    private BigDecimal positiveReviewPercentage;
}
