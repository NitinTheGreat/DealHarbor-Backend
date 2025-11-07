package com.dealharbor.dealharbor_backend.entities;

import com.dealharbor.dealharbor_backend.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity to track products that have been pending for more than 14 days
 * and moved to a review queue for admin action
 */
@Entity
@Table(name = "product_pending_reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPendingReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "original_created_at", nullable = false)
    private Instant originalCreatedAt;
    
    @Column(name = "moved_to_review_at", nullable = false)
    private Instant movedToReviewAt;
    
    @Column(name = "days_pending", nullable = false)
    private Integer daysPending;
    
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;
    
    @Column(name = "user_notified", nullable = false)
    @Builder.Default
    private Boolean userNotified = false;
    
    @Column(name = "notification_sent_at")
    private Instant notificationSentAt;
    
    @Column(name = "reviewed_at")
    private Instant reviewedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "review_decision")
    private ProductStatus reviewDecision;
    
    @Column(name = "review_reason", columnDefinition = "TEXT")
    private String reviewReason;
    
    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    private Boolean isResolved = false;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at")
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
