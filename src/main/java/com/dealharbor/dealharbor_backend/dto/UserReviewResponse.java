package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class UserReviewResponse {
    private String id;
    private String reviewerId;
    private String reviewerName;
    private String reviewerProfilePhoto;
    private ReviewType reviewType;
    private BigDecimal rating;
    private String comment;
    private BigDecimal communicationRating;
    private BigDecimal reliabilityRating;
    private BigDecimal speedRating;
    private String orderId;
    private String productTitle;
    private Instant createdAt;
}
