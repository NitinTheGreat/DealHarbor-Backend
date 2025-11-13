package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.services.ProductPendingReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-reviews")
@RequiredArgsConstructor
public class ProductPendingReviewController {
    
    private final ProductPendingReviewService pendingReviewService;
    
    /**
     * Get all products in pending review queue (Admin only)
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<ProductPendingReviewResponse>> getAllPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(pendingReviewService.getAllPendingReviews(page, size));
    }
    
    /**
     * Get count of products in pending review queue (Admin only)
     */
    @GetMapping("/admin/pending/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getPendingReviewCount() {
        return ResponseEntity.ok(pendingReviewService.getPendingReviewCount());
    }
    
    /**
     * Admin takes action on a product in review queue
     */
    @PostMapping("/admin/{reviewId}/action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> adminReviewProduct(
            @PathVariable String reviewId,
            @RequestBody PendingReviewActionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(pendingReviewService.adminReviewPendingProduct(reviewId, request, authentication));
    }
    
    /**
     * Get user's own products in review queue
     */
    @GetMapping("/my-pending")
    public ResponseEntity<List<ProductPendingReviewResponse>> getMyPendingReviews(Authentication authentication) {
        return ResponseEntity.ok(pendingReviewService.getUserPendingReviews(authentication));
    }
}
