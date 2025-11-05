package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.entities.*;
import com.dealharbor.dealharbor_backend.enums.*;
import com.dealharbor.dealharbor_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ProductReviewRepository productReviewRepository;
    private final UserReviewRepository userReviewRepository;
    private final AdminActionRepository adminActionRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    // ✅ ADMIN DASHBOARD
    public AdminDashboardResponse getDashboardStats() {
        Instant todayStart = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        
        return new AdminDashboardResponse(
                // User Statistics
                userRepository.count(),
                userRepository.countByDeletedFalseAndEnabledTrue(),
                userRepository.countByIsBannedTrue(),
                userRepository.countByIsVerifiedStudentTrue(),
                
                // Product Statistics
                productRepository.count(),
                productRepository.countByStatus(ProductStatus.PENDING),
                productRepository.countByStatus(ProductStatus.APPROVED),
                productRepository.countByStatus(ProductStatus.SOLD),
                productRepository.countByStatusAndIsFeaturedTrue(ProductStatus.APPROVED),
                
                // Order Statistics
                orderRepository.count(),
                orderRepository.countByStatus(OrderStatus.PENDING),
                orderRepository.countByStatus(OrderStatus.COMPLETED),
                orderRepository.countByStatus(OrderStatus.CANCELLED),
                
                // Review Statistics
                productReviewRepository.count() + userReviewRepository.count(),
                productReviewRepository.countByIsApprovedFalse() + userReviewRepository.countByIsApprovedFalse(),
                
                // Today's Activity
                userRepository.countByCreatedAtAfter(todayStart),
                productRepository.countByCreatedAtAfter(todayStart),
                orderRepository.countByCreatedAtAfter(todayStart)
        );
    }

    // ✅ ADMIN PRODUCT MANAGEMENT
    @Transactional
    public ProductResponse adminUpdateProduct(String productId, AdminProductActionRequest request, Authentication authentication) {
        User admin = getAdminFromAuthentication(authentication);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        ProductStatus oldStatus = product.getStatus();

        boolean statusChanged = request.getStatus() != null && request.getStatus() != oldStatus;
        boolean featuredChanged = request.getIsFeatured() != null && (product.getIsFeatured() != request.getIsFeatured());
        boolean notesChanged = request.getAdminNotes() != null && !request.getAdminNotes().equals(product.getAdminNotes());

        // Update product status
        if (statusChanged) {
            product.setStatus(request.getStatus());
            product.setApprovedBy(admin);
            product.setApprovedAt(Instant.now());
        }

        // Update featured status
        if (featuredChanged) {
            product.setIsFeatured(request.getIsFeatured());
        }

        // Update admin notes
        if (request.getAdminNotes() != null) {
            product.setAdminNotes(request.getAdminNotes());
        }

        product.setUpdatedAt(Instant.now());
        product = productRepository.save(product);

        // Record admin action(s)
        if (statusChanged) {
            recordAdminAction(admin, "PRODUCT_" + request.getStatus(), "PRODUCT", productId, request.getReason());
        }
        if (featuredChanged) {
            recordAdminAction(admin, request.getIsFeatured() ? "PRODUCT_FEATURE_ON" : "PRODUCT_FEATURE_OFF", "PRODUCT", productId, null);
        }
        if (notesChanged && !statusChanged && !featuredChanged) {
            recordAdminAction(admin, "PRODUCT_NOTE_UPDATED", "PRODUCT", productId, request.getAdminNotes());
        }

        // Notify seller only when status changes to a meaningful state
        if (statusChanged) {
            String notificationTitle = getProductNotificationTitle(request.getStatus());
            String notificationMessage = getProductNotificationMessage(product.getTitle(), request.getStatus(), request.getReason());

            notificationService.createNotification(
                    product.getSeller().getId(),
                    notificationTitle,
                    notificationMessage,
                    getProductNotificationType(request.getStatus()),
                    "/products/" + productId,
                    productId,
                    "PRODUCT"
            );

            // Send email for important status changes
            if (request.getStatus() == ProductStatus.APPROVED || request.getStatus() == ProductStatus.REJECTED) {
                emailService.sendProductStatusUpdate(
                        product.getSeller().getEmail(),
                        product.getSeller().getName(),
                        product.getTitle(),
                        request.getStatus().getDisplayName(),
                        request.getReason()
                );
            }
        }
        
        return convertToProductResponse(product);
    }

    public PagedResponse<ProductResponse> getAllProductsForAdmin(ProductStatus status, int page, int size, String sortBy) {
        Sort sort = createSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage;
        if (status != null) {
            productPage = productRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }
        
        return convertToPagedProductResponse(productPage);
    }

    // ✅ ADMIN USER MANAGEMENT
    @Transactional
    public UserProfileResponse adminUpdateUser(String userId, AdminUserActionRequest request, Authentication authentication) {
        User admin = getAdminFromAuthentication(authentication);
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        switch (request.getAction()) {
            case "BAN" -> {
                targetUser.setBanned(true);
                targetUser.setBannedUntil(request.getBannedUntil());
                targetUser.setBanReason(request.getReason());
                targetUser.setEnabled(false);
                
                // Send ban notification
                notificationService.createNotification(
                        userId,
                        "Account Banned",
                        "Your account has been banned. Reason: " + request.getReason(),
                        NotificationType.SECURITY_ALERT,
                        null, null, "USER"
                );
                
                emailService.sendAccountBanNotification(
                        targetUser.getEmail(),
                        targetUser.getName(),
                        request.getReason(),
                        request.getBannedUntil()
                );
            }
            case "UNBAN" -> {
                targetUser.setBanned(false);
                targetUser.setBannedUntil(null);
                targetUser.setBanReason(null);
                targetUser.setEnabled(true);
                
                notificationService.createNotification(
                        userId,
                        "Account Unbanned",
                        "Your account has been unbanned. You can now access all features.",
                        NotificationType.ACCOUNT_VERIFIED,
                        null, null, "USER"
                );
            }
            case "VERIFY_STUDENT" -> {
                targetUser.setVerifiedStudent(request.getIsVerifiedStudent());
                
                if (request.getIsVerifiedStudent()) {
                    notificationService.createNotification(
                            userId,
                            "Student Verification Approved",
                            "Your student verification has been approved by admin.",
                            NotificationType.ACCOUNT_VERIFIED,
                            null, null, "USER"
                    );
                }
            }
            case "MARK_SPAM" -> {
                targetUser.setBanned(true);
                targetUser.setBanReason("Marked as spam/fishy by admin: " + request.getReason());
                targetUser.setEnabled(false);
                
                // Also mark all their products as deleted
                List<Product> userProducts = productRepository.findBySellerIdAndStatusNot(userId, ProductStatus.DELETED);
                userProducts.forEach(product -> {
                    product.setStatus(ProductStatus.DELETED);
                    product.setAdminNotes("User marked as spam");
                });
                productRepository.saveAll(userProducts);
            }
            case "DELETE_ACCOUNT" -> {
                targetUser.setDeleted(true);
                targetUser.setDeletedAt(Instant.now());
                targetUser.setEnabled(false);
                
                // Mark all products as deleted
                List<Product> userProducts = productRepository.findBySellerIdAndStatusNot(userId, ProductStatus.DELETED);
                userProducts.forEach(product -> product.setStatus(ProductStatus.DELETED));
                productRepository.saveAll(userProducts);
            }
        }
        
        targetUser.setUpdatedAt(Instant.now());
        userRepository.save(targetUser);
        
        // Record admin action
        recordAdminAction(admin, request.getAction(), "USER", userId, request.getReason());
        
        return convertToUserProfileResponse(targetUser);
    }

    public PagedResponse<UserProfileResponse> getAllUsersForAdmin(String filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<User> userPage = switch (filter) {
            case "banned" -> userRepository.findByIsBannedTrue(pageable);
            case "verified" -> userRepository.findByIsVerifiedStudentTrue(pageable);
            case "deleted" -> userRepository.findByDeletedTrue(pageable);
            default -> userRepository.findAll(pageable);
        };
        
        List<UserProfileResponse> content = userPage.getContent().stream()
                .map(this::convertToUserProfileResponse)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isFirst(),
                userPage.isLast(),
                userPage.hasNext(),
                userPage.hasPrevious()
        );
    }

    // ✅ ADMIN SEARCH FUNCTIONALITY
    public PagedResponse<ProductResponse> searchProductsForAdmin(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.searchByKeywordForAdmin(keyword, pageable);
        return convertToPagedProductResponse(productPage);
    }

    public PagedResponse<UserProfileResponse> searchUsersForAdmin(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.searchByKeywordForAdmin(keyword, pageable);
        
        List<UserProfileResponse> content = userPage.getContent().stream()
                .map(this::convertToUserProfileResponse)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isFirst(),
                userPage.isLast(),
                userPage.hasNext(),
                userPage.hasPrevious()
        );
    }

    // Helper methods
    private void recordAdminAction(User admin, String actionType, String targetType, String targetId, String reason) {
        AdminAction action = AdminAction.builder()
                .admin(admin)
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .reason(reason)
                .build();
        adminActionRepository.save(action);
    }

    private String getProductNotificationTitle(ProductStatus status) {
        return switch (status) {
            case APPROVED -> "Product Approved";
            case REJECTED -> "Product Rejected";
            case DELETED -> "Product Removed";
            default -> "Product Status Updated";
        };
    }

    private String getProductNotificationMessage(String productTitle, ProductStatus status, String reason) {
        String baseMessage = "Your product '" + productTitle + "' has been " + status.getDisplayName().toLowerCase();
        return reason != null ? baseMessage + ". Reason: " + reason : baseMessage + ".";
    }

    private NotificationType getProductNotificationType(ProductStatus status) {
        return switch (status) {
            case APPROVED -> NotificationType.PRODUCT_APPROVED;
            case REJECTED -> NotificationType.PRODUCT_REJECTED;
            default -> NotificationType.SYSTEM_ANNOUNCEMENT;
        };
    }

    private Sort createSort(String sortBy) {
        return switch (sortBy) {
            case "date_asc" -> Sort.by("createdAt").ascending();
            case "date_desc" -> Sort.by("createdAt").descending();
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            default -> Sort.by("createdAt").descending();
        };
    }

    private PagedResponse<ProductResponse> convertToPagedProductResponse(Page<Product> productPage) {
        List<ProductResponse> content = productPage.getContent().stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isFirst(),
                productPage.isLast(),
                productPage.hasNext(),
                productPage.hasPrevious()
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

    private UserProfileResponse convertToUserProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getBio(),
                user.getPhoneNumber(),
                user.getProfilePhotoUrl(),
                user.getRole(),
                user.isEnabled(),
                user.isLocked(),
                user.getProvider(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getSellerBadge(),
                user.getSellerRating(),
                user.getBuyerRating(),
                user.getTotalSales(),
                user.getTotalPurchases(),
                user.getTotalListings(),
                user.getActiveListings(),
                user.getTotalRevenue(),
                user.getResponseRate(),
                user.getPositiveReviews(),
                user.getNegativeReviews(),
                user.getFirstSaleAt(),
                user.getUniversityId(),
                user.getGraduationYear(),
                user.getDepartment(),
                user.isVerifiedStudent(),
                user.getOverallRating(),
                user.getSellerSuccessRate()
        );
    }

    private User getAdminFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User admin = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        if (admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Access denied. Admin privileges required.");
        }
        
        return admin;
    }
}
