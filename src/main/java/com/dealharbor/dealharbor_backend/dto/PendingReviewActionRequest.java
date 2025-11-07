package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingReviewActionRequest {
    private ProductStatus decision; // APPROVED or REJECTED
    private String reason;
    private String adminNotes;
}
