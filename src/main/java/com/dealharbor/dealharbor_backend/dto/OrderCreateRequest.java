package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.DeliveryMethod;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCreateRequest {
    private String productId;
    private BigDecimal agreedPrice;
    private String buyerNotes;
    private String pickupLocation;
    private DeliveryMethod deliveryMethod;
}
