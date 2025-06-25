package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.ProductCondition;
import com.dealharbor.dealharbor_backend.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class ProductResponse {
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private boolean isNegotiable;
    private ProductCondition condition;
    private String brand;
    private String model;
    private ProductStatus status;
    private String pickupLocation;
    private boolean deliveryAvailable;
    private Integer viewCount;
    private Integer favoriteCount;
    private boolean isFeatured;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Seller info
    private String sellerId;
    private String sellerName;
    private String sellerBadge;
    private BigDecimal sellerRating;
    private boolean sellerIsVerifiedStudent;
    
    // Category info
    private String categoryId;
    private String categoryName;
    
    // Images
    private List<ProductImageResponse> images;
    private String primaryImageUrl;
}
