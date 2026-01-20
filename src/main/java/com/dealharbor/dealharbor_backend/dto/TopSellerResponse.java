package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSellerResponse {
    private String id;
    private String name;
    private String profilePhotoUrl;
    private BigDecimal rating;
    private int reviewCount;
    private int totalSales;
    private boolean isVerified;
    private String badge;
    private Instant joinedAt;
}
