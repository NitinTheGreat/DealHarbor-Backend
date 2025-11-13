package com.dealharbor.dealharbor_backend.entities;

import com.dealharbor.dealharbor_backend.enums.ProductCondition;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "sold_products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoldProduct {
    @Id
    private String id; // Keep original product ID

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal soldPrice; // Actual selling price (may differ from listed price)

    @Column(nullable = false)
    private Boolean isNegotiable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCondition condition;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String model;

    // Store IDs instead of relationships to avoid foreign key constraints
    @Column(nullable = false)
    private String categoryId;

    @Column(nullable = false)
    private String categoryName;

    @Column(nullable = false)
    private String sellerId;

    @Column(nullable = false)
    private String sellerName;

    @Column
    private String buyerId; // Who bought it

    @Column
    private String buyerName;

    // Images stored as JSON array
    @Column(columnDefinition = "TEXT")
    private String imageUrls; // JSON array of image URLs

    @Column
    private String primaryImageUrl;

    // Tags
    @Column(columnDefinition = "TEXT")
    private String tags;

    // Metrics
    @Column(nullable = false)
    private Integer viewCount;

    @Column(nullable = false)
    private Integer favoriteCount;

    // Location
    @Column(length = 200)
    private String pickupLocation;

    @Column(nullable = false)
    private Boolean deliveryAvailable;

    // Timestamps
    @Column(nullable = false)
    private Instant createdAt; // Original creation date

    @Column(nullable = false)
    private Instant soldAt; // When it was marked as sold

    @Column(nullable = false)
    private Instant archivedAt; // When it was moved to sold_products table

    @PrePersist
    protected void onCreate() {
        archivedAt = Instant.now();
    }
}
