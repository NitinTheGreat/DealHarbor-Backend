package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.ProductPendingReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPendingReviewRepository extends JpaRepository<ProductPendingReview, String> {
    
    // Find by product ID
    Optional<ProductPendingReview> findByProductId(String productId);
    
    // Find all unresolved reviews
    Page<ProductPendingReview> findByIsResolvedFalse(Pageable pageable);
    
    // Find all resolved reviews
    Page<ProductPendingReview> findByIsResolvedTrue(Pageable pageable);
    
    // Count unresolved reviews
    long countByIsResolvedFalse();
    
    // Find by seller (through product relationship)
    @Query("SELECT pr FROM ProductPendingReview pr WHERE pr.product.seller.id = :sellerId AND pr.isResolved = false")
    List<ProductPendingReview> findBySellerIdAndUnresolved(String sellerId);
    
    // Check if product already in review
    boolean existsByProductIdAndIsResolvedFalse(String productId);
    
    // Find all unresolved (for admin)
    List<ProductPendingReview> findByIsResolvedFalse();
}
