package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.ProductStatus;
import lombok.Data;

@Data
public class AdminProductActionRequest {
    private ProductStatus status; // APPROVED, REJECTED, DELETED
    private String reason; // Reason for action
    private Boolean isFeatured; // Make product featured
    private String adminNotes; // Admin notes
}
