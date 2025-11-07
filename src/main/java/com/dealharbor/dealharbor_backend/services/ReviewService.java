package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.entities.*;
import com.dealharbor.dealharbor_backend.enums.OrderStatus;
import com.dealharbor.dealharbor_backend.enums.ReviewType;
import com.dealharbor.dealharbor_backend.enums.NotificationType;
import com.dealharbor.dealharbor_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ProductReviewRepository productReviewRepository;
    private final UserReviewRepository userReviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ProductReviewResponse createProductReview(ProductReviewRequest request, Authentication authentication) {
        User reviewer = getUserFromAuthentication(authentication);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (productReviewRepository.existsByReviewerIdAndProductId(reviewer.getId(), request.getProductId())) {
            throw new RuntimeException("You have already reviewed this product");
        }
        
        if (product.getSeller().getId().equals(reviewer.getId())) {
            throw new RuntimeException("Cannot review your own product");
        }
        
        // Check if it's a verified purchase
        Order order = null;
        boolean isVerifiedPurchase = false;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId()).orElse(null);
            if (order != null && order.getBuyer().getId().equals(reviewer.getId()) && 
                order.getProduct().getId().equals(request.getProductId()) &&
                order.getStatus() == OrderStatus.COMPLETED) {
                isVerifiedPurchase = true;
            }
        }
        
        ProductReview review = ProductReview.builder()
                .reviewer(reviewer)
                .product(product)
                .order(order)
                .rating(request.getRating())
                .comment(request.getComment())
                .isVerifiedPurchase(isVerifiedPurchase)
                .build();
        
        review = productReviewRepository.save(review);
        
        // Update product rating
        updateProductRating(product);
        
        // Notify seller
        notificationService.createNotification(
                product.getSeller().getId(),
                "New Product Review",
                reviewer.getName() + " reviewed your product '" + product.getTitle() + "'",
                NotificationType.NEW_REVIEW,
                "/products/" + product.getId(),
                product.getId(),
                "PRODUCT"
        );
        
        return convertToProductReviewResponse(review);
    }

    @Transactional
    public UserReviewResponse createUserReview(UserReviewRequest request, Authentication authentication) {
        User reviewer = getUserFromAuthentication(authentication);
        User reviewee = userRepository.findById(request.getRevieweeId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (userReviewRepository.existsByReviewerIdAndOrderId(reviewer.getId(), request.getOrderId())) {
            throw new RuntimeException("You have already reviewed this user for this order");
        }
        
        // Validate review permissions
        if (request.getReviewType() == ReviewType.SELLER_REVIEW) {
            if (!order.getBuyer().getId().equals(reviewer.getId()) || 
                !order.getSeller().getId().equals(reviewee.getId())) {
                throw new RuntimeException("Invalid seller review permissions");
            }
        } else if (request.getReviewType() == ReviewType.BUYER_REVIEW) {
            if (!order.getSeller().getId().equals(reviewer.getId()) || 
                !order.getBuyer().getId().equals(reviewee.getId())) {
                throw new RuntimeException("Invalid buyer review permissions");
            }
        }
        
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Can only review after order completion");
        }
        
        UserReview review = UserReview.builder()
                .reviewer(reviewer)
                .reviewee(reviewee)
                .order(order)
                .reviewType(request.getReviewType())
                .rating(request.getRating())
                .comment(request.getComment())
                .communicationRating(request.getCommunicationRating())
                .reliabilityRating(request.getReliabilityRating())
                .speedRating(request.getSpeedRating())
                .build();
        
        review = userReviewRepository.save(review);
        
        // Update user ratings
        updateUserRating(reviewee, request.getReviewType());
        
        // Notify reviewee
        String reviewTypeText = request.getReviewType() == ReviewType.SELLER_REVIEW ? "seller" : "buyer";
        notificationService.createNotification(
                reviewee.getId(),
                "New " + reviewTypeText + " Review",
                reviewer.getName() + " reviewed you as a " + reviewTypeText,
                NotificationType.NEW_REVIEW,
                "/profile/" + reviewee.getId(),
                reviewee.getId(),
                "USER"
        );
        
        return convertToUserReviewResponse(review);
    }

    public PagedResponse<ProductReviewResponse> getProductReviews(String productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductReview> reviewPage = productReviewRepository
                .findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(productId, pageable);
        
        List<ProductReviewResponse> content = reviewPage.getContent().stream()
                .map(this::convertToProductReviewResponse)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.isFirst(),
                reviewPage.isLast(),
                reviewPage.hasNext(),
                reviewPage.hasPrevious()
        );
    }

    public PagedResponse<UserReviewResponse> getUserReviews(String userId, ReviewType reviewType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserReview> reviewPage = userReviewRepository
                .findByRevieweeIdAndReviewTypeAndIsApprovedTrueOrderByCreatedAtDesc(userId, reviewType, pageable);
        
        List<UserReviewResponse> content = reviewPage.getContent().stream()
                .map(this::convertToUserReviewResponse)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.isFirst(),
                reviewPage.isLast(),
                reviewPage.hasNext(),
                reviewPage.hasPrevious()
        );
    }

    private void updateProductRating(Product product) {
        BigDecimal averageRating = productReviewRepository.getAverageRatingByProductId(product.getId());
        // Note: Product rating would need to be added to Product entity
        // For now, we'll skip this or you can add a rating field to Product
    }

    private void updateUserRating(User user, ReviewType reviewType) {
        BigDecimal averageRating = userReviewRepository.getAverageRatingByUserAndType(user.getId(), reviewType);
        long reviewCount = userReviewRepository.countByUserAndType(user.getId(), reviewType);
        
        if (reviewType == ReviewType.SELLER_REVIEW) {
            user.setSellerRating(averageRating != null ? averageRating : BigDecimal.ZERO);
            // Update positive/negative review counts based on rating
            if (averageRating != null && averageRating.compareTo(BigDecimal.valueOf(3.5)) >= 0) {
                user.setPositiveReviews(user.getPositiveReviews() + 1);
            } else {
                user.setNegativeReviews(user.getNegativeReviews() + 1);
            }
        } else {
            user.setBuyerRating(averageRating != null ? averageRating : BigDecimal.ZERO);
        }
        
        userRepository.save(user);
    }

    private ProductReviewResponse convertToProductReviewResponse(ProductReview review) {
        return new ProductReviewResponse(
                review.getId(),
                review.getReviewer().getId(),
                review.getReviewer().getName(),
                review.getReviewer().getProfilePhotoUrl(),
                review.getReviewer().isVerifiedStudent(),
                review.getRating(),
                review.getComment(),
                review.getIsVerifiedPurchase(),
                review.getIsHelpful(),
                review.getHelpfulCount(),
                review.getCreatedAt()
        );
    }

    private UserReviewResponse convertToUserReviewResponse(UserReview review) {
        return new UserReviewResponse(
                review.getId(),
                review.getReviewer().getId(),
                review.getReviewer().getName(),
                review.getReviewer().getProfilePhotoUrl(),
                review.getReviewType(),
                review.getRating(),
                review.getComment(),
                review.getCommunicationRating(),
                review.getReliabilityRating(),
                review.getSpeedRating(),
                review.getOrder().getId(),
                review.getOrder().getProductTitle(),
                review.getCreatedAt()
        );
    }

    private User getUserFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
