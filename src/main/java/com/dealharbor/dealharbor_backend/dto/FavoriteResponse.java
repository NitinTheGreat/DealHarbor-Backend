package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class FavoriteResponse {
    private String id;
    private String productId;
    private String productTitle;
    private String productImageUrl;
    private String productPrice;
    private String productStatus;
    private String sellerId;
    private String sellerName;
    private Instant createdAt;
}
