package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.OrderStatus;
import com.dealharbor.dealharbor_backend.enums.DeliveryMethod;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal agreedPrice;
    private BigDecimal originalPrice;
    private String buyerNotes;
    private String sellerNotes;
    private String pickupLocation;
    private DeliveryMethod deliveryMethod;
    private Instant createdAt;
    private Instant confirmedAt;
    private Instant completedAt;
    
    // Product info
    private String productId;
    private String productTitle;
    private String productImageUrl;
    
    // Buyer info
    private String buyerId;
    private String buyerName;
    
    // Seller info
    private String sellerId;
    private String sellerName;
}
