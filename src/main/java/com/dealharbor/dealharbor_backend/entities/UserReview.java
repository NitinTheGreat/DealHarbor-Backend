package com.dealharbor.dealharbor_backend.entities;

import com.dealharbor.dealharbor_backend.enums.ReviewType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "user_reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"reviewer_id", "reviewee_id", "order_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReview {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType reviewType; // SELLER_REVIEW, BUYER_REVIEW

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating; // 1.0 to 5.0

    @Column(columnDefinition = "TEXT")
    private String comment;

    // Specific rating aspects
    @Column(precision = 2, scale = 1)
    private BigDecimal communicationRating;

    @Column(precision = 2, scale = 1)
    private BigDecimal reliabilityRating;

    @Column(precision = 2, scale = 1)
    private BigDecimal speedRating;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isApproved = true;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
