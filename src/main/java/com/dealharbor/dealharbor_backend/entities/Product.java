package com.dealharbor.dealharbor_backend.entities;

import com.dealharbor.dealharbor_backend.enums.ProductCondition;
import com.dealharbor.dealharbor_backend.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isNegotiable = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductCondition condition = ProductCondition.USED;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String model;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> images;

    // Search and categorization
    @Column(columnDefinition = "TEXT")
    private String tags; // JSON array of tags

    // Status management
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    private Instant approvedAt;

    // Metrics
    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer favoriteCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    // Location and delivery
    @Column(length = 200)
    private String pickupLocation;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deliveryAvailable = false;

    // Timestamps
    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Instant soldAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Helper method to get primary image
    public ProductImage getPrimaryImage() {
        if (images == null || images.isEmpty()) return null;
        return images.stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .orElse(images.get(0));
    }
}
