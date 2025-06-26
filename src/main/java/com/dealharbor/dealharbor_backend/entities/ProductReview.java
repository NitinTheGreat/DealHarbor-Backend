package com.dealharbor.dealharbor_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product_reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"reviewer_id", "product_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // Link to order for verified purchases

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating; // 1.0 to 5.0

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerifiedPurchase = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isHelpful = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer helpfulCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isReported = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isApproved = true;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
