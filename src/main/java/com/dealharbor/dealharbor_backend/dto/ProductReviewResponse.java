package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class ProductReviewResponse {
    private String id;
    private String reviewerId;
    private String reviewerName;
    private String reviewerProfilePhoto;
    private boolean reviewerIsVerifiedStudent;
    private BigDecimal rating;
    private String comment;
    private boolean isVerifiedPurchase;
    private boolean isHelpful;
    private int helpfulCount;
    private Instant createdAt;
}
