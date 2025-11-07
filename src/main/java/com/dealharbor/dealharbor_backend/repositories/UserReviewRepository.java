package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.UserReview;
import com.dealharbor.dealharbor_backend.enums.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface UserReviewRepository extends JpaRepository<UserReview, String> {
    Page<UserReview> findByRevieweeIdAndReviewTypeAndIsApprovedTrueOrderByCreatedAtDesc(
        String revieweeId, ReviewType reviewType, Pageable pageable);
    
    Optional<UserReview> findByReviewerIdAndOrderId(String reviewerId, String orderId);
    boolean existsByReviewerIdAndOrderId(String reviewerId, String orderId);
    
    @Query("SELECT AVG(r.rating) FROM UserReview r WHERE r.reviewee.id = :userId AND r.reviewType = :reviewType AND r.isApproved = true")
    BigDecimal getAverageRatingByUserAndType(String userId, ReviewType reviewType);
    
    @Query("SELECT COUNT(r) FROM UserReview r WHERE r.reviewee.id = :userId AND r.reviewType = :reviewType AND r.isApproved = true")
    long countByUserAndType(String userId, ReviewType reviewType);
    
    // Admin queries
    long countByIsApprovedFalse();
}
