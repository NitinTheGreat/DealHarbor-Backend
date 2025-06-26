package com.dealharbor.dealharbor_backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductReviewRequest {
    private String productId;
    private String orderId; // Optional - for verified purchase
    private BigDecimal rating; // 1.0 to 5.0
    private String comment;
}
