package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPendingReviewResponse {
    private String id;
    private String productId;
    private String productTitle;
    private String productDescription;
    private Double productPrice;
    private String categoryName;
    private String sellerName;
    private String sellerEmail;
    private Instant originalCreatedAt;
    private Instant movedToReviewAt;
    private Integer daysPending;
    private String reviewNotes;
    private Boolean userNotified;
    private Instant notificationSentAt;
    private Instant reviewedAt;
    private String reviewedByName;
    private ProductStatus reviewDecision;
    private String reviewReason;
    private Boolean isResolved;
    private Instant createdAt;
    private Instant updatedAt;
}
