package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.enums.ReviewType;
import com.dealharbor.dealharbor_backend.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {
    
    private final ReviewService reviewService;

    // Product Reviews
    @PostMapping("/products")
    public ResponseEntity<ProductReviewResponse> createProductReview(
            @RequestBody ProductReviewRequest request, 
            Authentication authentication) {
        return ResponseEntity.ok(reviewService.createProductReview(request, authentication));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<PagedResponse<ProductReviewResponse>> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, page, size));
    }

    // User Reviews
    @PostMapping("/users")
    public ResponseEntity<UserReviewResponse> createUserReview(
            @RequestBody UserReviewRequest request, 
            Authentication authentication) {
        return ResponseEntity.ok(reviewService.createUserReview(request, authentication));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<PagedResponse<UserReviewResponse>> getUserReviews(
            @PathVariable String userId,
            @RequestParam ReviewType reviewType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getUserReviews(userId, reviewType, page, size));
    }
}
