package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleanupStatsResponse {
    private long rejectedProductsCount;
    private long oldPendingProductsCount;
    private long totalProductsToDelete;
    private String oldestPendingProductDate;
    private String message;
}
