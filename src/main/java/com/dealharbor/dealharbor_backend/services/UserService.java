package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.SellerProfileResponse;
import com.dealharbor.dealharbor_backend.entities.User;
import com.dealharbor.dealharbor_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Get seller profile by user ID
     * Returns public information about a seller
     */
    public SellerProfileResponse getSellerProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        // Calculate total reviews
        Integer totalReviews = user.getPositiveReviews() + user.getNegativeReviews();
        
        // Calculate positive review percentage
        BigDecimal positiveReviewPercentage = BigDecimal.ZERO;
        if (totalReviews > 0) {
            positiveReviewPercentage = BigDecimal.valueOf(user.getPositiveReviews())
                    .divide(BigDecimal.valueOf(totalReviews), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        
        return SellerProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .bio(user.getBio())
                .phoneNumber(user.getPhoneNumber()) // Only visible to authenticated users
                .isVerifiedStudent(user.isVerifiedStudent())
                .department(user.getDepartment())
                .graduationYear(user.getGraduationYear())
                .sellerRating(user.getSellerRating())
                .buyerRating(user.getBuyerRating())
                .overallRating(user.getOverallRating())
                .totalSales(user.getTotalSales())
                .totalPurchases(user.getTotalPurchases())
                .totalListings(user.getTotalListings())
                .activeListings(user.getActiveListings())
                .sellerBadge(user.getSellerBadge())
                .firstSaleAt(user.getFirstSaleAt())
                .totalRevenue(user.getTotalRevenue())
                .responseRate(user.getResponseRate())
                .positiveReviews(user.getPositiveReviews())
                .negativeReviews(user.getNegativeReviews())
                .successRate(user.getSellerSuccessRate())
                .memberSince(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .totalReviews(totalReviews)
                .positiveReviewPercentage(positiveReviewPercentage)
                .build();
    }
    
    /**
     * Get public seller profile (without sensitive information)
     * Used for unauthenticated users viewing seller profiles
     */
    public SellerProfileResponse getPublicSellerProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        // Calculate total reviews
        Integer totalReviews = user.getPositiveReviews() + user.getNegativeReviews();
        
        // Calculate positive review percentage
        BigDecimal positiveReviewPercentage = BigDecimal.ZERO;
        if (totalReviews > 0) {
            positiveReviewPercentage = BigDecimal.valueOf(user.getPositiveReviews())
                    .divide(BigDecimal.valueOf(totalReviews), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        
        // Return public info only (no phone, no email, no revenue)
        return SellerProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .bio(user.getBio())
                .phoneNumber(null) // Hidden for public view
                .isVerifiedStudent(user.isVerifiedStudent())
                .department(user.getDepartment())
                .graduationYear(user.getGraduationYear())
                .sellerRating(user.getSellerRating())
                .buyerRating(user.getBuyerRating())
                .overallRating(user.getOverallRating())
                .totalSales(user.getTotalSales())
                .totalPurchases(user.getTotalPurchases())
                .totalListings(user.getTotalListings())
                .activeListings(user.getActiveListings())
                .sellerBadge(user.getSellerBadge())
                .firstSaleAt(user.getFirstSaleAt())
                .totalRevenue(null) // Hidden for public view
                .responseRate(user.getResponseRate())
                .positiveReviews(user.getPositiveReviews())
                .negativeReviews(user.getNegativeReviews())
                .successRate(user.getSellerSuccessRate())
                .memberSince(user.getCreatedAt())
                .lastLoginAt(null) // Hidden for public view
                .totalReviews(totalReviews)
                .positiveReviewPercentage(positiveReviewPercentage)
                .build();
    }
}
