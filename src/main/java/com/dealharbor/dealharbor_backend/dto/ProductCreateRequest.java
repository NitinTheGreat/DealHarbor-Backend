package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.ProductCondition;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCreateRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private boolean isNegotiable;
    private ProductCondition condition;
    private String brand;
    private String model;
    private String categoryId;
    private List<String> tags;
    private String pickupLocation;
    private boolean deliveryAvailable;
    private List<String> imageUrls; // URLs of uploaded images
}
