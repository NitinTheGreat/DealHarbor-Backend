package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductImageResponse {
    private String id;
    private String imageUrl;
    private String altText;
    private boolean isPrimary;
    private Integer sortOrder;
}
