package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.entities.*;
import com.dealharbor.dealharbor_backend.enums.*;
import com.dealharbor.dealharbor_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductPendingReviewService {
    
    private final ProductPendingReviewRepository pendingReviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final AdminActionRepository adminActionRepository;
    
    // Get all pending reviews for admin
    public PagedResponse<ProductPendingReviewResponse> getAllPendingReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("movedToReviewAt").descending());
        Page<ProductPendingReview> reviewPage = pendingReviewRepository.findByIsResolvedFalse(pageable);
        return convertToPagedResponse(reviewPage);
    }
    
    // Get user's own products in review queue
    public List<ProductPendingReviewResponse> getUserPendingReviews(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        List<ProductPendingReview> reviews = pendingReviewRepository.findBySellerIdAndUnresolved(user.getId());
        return reviews.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // Admin takes action on pending review
    @Transactional
    public ProductResponse adminReviewPendingProduct(String reviewId, PendingReviewActionRequest request, Authentication authentication) {
        User admin = getAdminFromAuthentication(authentication);
        
        ProductPendingReview review = pendingReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Pending review not found"));
        
        if (review.getIsResolved()) {
            throw new RuntimeException("This review has already been resolved");
        }
        
        Product product = review.getProduct();
        User seller = product.getSeller();
        
        if (request.getDecision() == ProductStatus.APPROVED) {
            // Approve the product
            product.setStatus(ProductStatus.APPROVED);
            product.setApprovedBy(admin);
            product.setApprovedAt(Instant.now());
            if (request.getAdminNotes() != null) {
                product.setAdminNotes(request.getAdminNotes());
            }
            product.setUpdatedAt(Instant.now());
            productRepository.save(product);
            
            // Mark review as resolved
            review.setIsResolved(true);
            review.setReviewedAt(Instant.now());
            review.setReviewedBy(admin);
            review.setReviewDecision(ProductStatus.APPROVED);
            review.setReviewReason(request.getReason());
            pendingReviewRepository.save(review);
            
            // Send email notification
            try {
                emailService.sendProductApprovedAfterReview(
                        seller.getEmail(),
                        seller.getName(),
                        product.getTitle()
                );
            } catch (Exception e) {
                log.error("Failed to send approval email for product {}: {}", product.getId(), e.getMessage());
            }
            
            // Create notification
            notificationService.createNotification(
                    seller.getId(),
                    "Product Approved!",
                    "Your product '" + product.getTitle() + "' has been approved and is now live!",
                    NotificationType.PRODUCT_APPROVED,
                    "/products/" + product.getId(),
                    product.getId(),
                    "PRODUCT"
            );
            
            // Record admin action
            recordAdminAction(admin, "PRODUCT_APPROVED_FROM_REVIEW", "PRODUCT", product.getId(), 
                    "Approved after " + review.getDaysPending() + " days in review");
            
            log.info("Admin {} approved product {} from review queue", admin.getEmail(), product.getId());
            
        } else if (request.getDecision() == ProductStatus.REJECTED) {
            // Reject and delete the product
            product.setStatus(ProductStatus.REJECTED);
            product.setAdminNotes(request.getAdminNotes());
            productRepository.save(product);
            
            // Mark review as resolved
            review.setIsResolved(true);
            review.setReviewedAt(Instant.now());
            review.setReviewedBy(admin);
            review.setReviewDecision(ProductStatus.REJECTED);
            review.setReviewReason(request.getReason());
            pendingReviewRepository.save(review);
            
            // Send email notification
            try {
                emailService.sendProductRejectedAfterReview(
                        seller.getEmail(),
                        seller.getName(),
                        product.getTitle(),
                        request.getReason()
                );
            } catch (Exception e) {
                log.error("Failed to send rejection email for product {}: {}", product.getId(), e.getMessage());
            }
            
            // Create notification
            notificationService.createNotification(
                    seller.getId(),
                    "Product Rejected",
                    "Your product '" + product.getTitle() + "' has been rejected. Reason: " + request.getReason(),
                    NotificationType.PRODUCT_REJECTED,
                    null,
                    product.getId(),
                    "PRODUCT"
            );
            
            // Record admin action
            recordAdminAction(admin, "PRODUCT_REJECTED_FROM_REVIEW", "PRODUCT", product.getId(), 
                    request.getReason());
            
            // Update seller stats
            seller.setActiveListings(Math.max(0, seller.getActiveListings() - 1));
            userRepository.save(seller);
            
            log.info("Admin {} rejected product {} from review queue", admin.getEmail(), product.getId());
            
        } else {
            throw new RuntimeException("Invalid decision. Must be APPROVED or REJECTED");
        }
        
        return convertToProductResponse(product);
    }
    
    // Get count of pending reviews
    public long getPendingReviewCount() {
        return pendingReviewRepository.countByIsResolvedFalse();
    }
    
    // Helper methods
    private ProductPendingReviewResponse convertToResponse(ProductPendingReview review) {
        Product product = review.getProduct();
        return ProductPendingReviewResponse.builder()
                .id(review.getId())
                .productId(product.getId())
                .productTitle(product.getTitle())
                .productDescription(product.getDescription())
                .productPrice(product.getPrice().doubleValue())
                .categoryName(product.getCategory().getName())
                .sellerName(product.getSeller().getName())
                .sellerEmail(product.getSeller().getEmail())
                .originalCreatedAt(review.getOriginalCreatedAt())
                .movedToReviewAt(review.getMovedToReviewAt())
                .daysPending(review.getDaysPending())
                .reviewNotes(review.getReviewNotes())
                .userNotified(review.getUserNotified())
                .notificationSentAt(review.getNotificationSentAt())
                .reviewedAt(review.getReviewedAt())
                .reviewedByName(review.getReviewedBy() != null ? review.getReviewedBy().getName() : null)
                .reviewDecision(review.getReviewDecision())
                .reviewReason(review.getReviewReason())
                .isResolved(review.getIsResolved())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
    
    private PagedResponse<ProductPendingReviewResponse> convertToPagedResponse(Page<ProductPendingReview> page) {
        List<ProductPendingReviewResponse> content = page.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
    
    private ProductResponse convertToProductResponse(Product product) {
        List<ProductImageResponse> images = product.getImages() != null 
                ? product.getImages().stream()
                    .map(img -> new ProductImageResponse(
                            img.getId(),
                            img.getImageUrl(),
                            img.getAltText(),
                            img.getIsPrimary(),
                            img.getSortOrder()
                    ))
                    .collect(Collectors.toList())
                : List.of();
        
        String primaryImageUrl = images.stream()
                .filter(ProductImageResponse::isPrimary)
                .findFirst()
                .map(ProductImageResponse::getImageUrl)
                .orElse(images.isEmpty() ? null : images.get(0).getImageUrl());
        
        List<String> tags = product.getTags() != null 
                ? List.of(product.getTags().split(","))
                : List.of();
        
        return new ProductResponse(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getOriginalPrice(),
                product.getIsNegotiable(),
                product.getCondition(),
                product.getBrand(),
                product.getModel(),
                product.getStatus(),
                product.getPickupLocation(),
                product.getDeliveryAvailable(),
                product.getViewCount(),
                product.getFavoriteCount(),
                product.getIsFeatured(),
                tags,
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getSeller().getId(),
                product.getSeller().getName(),
                product.getSeller().getSellerBadge().name(),
                product.getSeller().getSellerRating(),
                product.getSeller().isVerifiedStudent(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                images,
                primaryImageUrl
        );
    }
    
    private void recordAdminAction(User admin, String actionType, String targetType, String targetId, String reason) {
        AdminAction action = AdminAction.builder()
                .admin(admin)
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .reason(reason)
                .details("{}")
                .build();
        adminActionRepository.save(action);
    }
    
    private User getUserFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private User getAdminFromAuthentication(Authentication authentication) {
        User admin = getUserFromAuthentication(authentication);
        if (admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Access denied. Admin privileges required.");
        }
        return admin;
    }
}
