package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, String> {
    Page<ProductReview> findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(String productId, Pageable pageable);
    Optional<ProductReview> findByReviewerIdAndProductId(String reviewerId, String productId);
    boolean existsByReviewerIdAndProductId(String reviewerId, String productId);
    
    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId AND r.isApproved = true")
    BigDecimal getAverageRatingByProductId(String productId);
    
    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.isApproved = true")
    long countByProductId(String productId);
    
    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.seller.id = :sellerId AND r.isApproved = true")
    long countReviewsBySellerProducts(String sellerId);
}
