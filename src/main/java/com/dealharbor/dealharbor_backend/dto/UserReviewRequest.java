package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.ReviewType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserReviewRequest {
    private String orderId;
    private String revieweeId;
    private ReviewType reviewType;
    private BigDecimal rating; // Overall rating 1.0 to 5.0
    private String comment;
    
    // Specific aspect ratings (optional)
    private BigDecimal communicationRating;
    private BigDecimal reliabilityRating;
    private BigDecimal speedRating;
}
