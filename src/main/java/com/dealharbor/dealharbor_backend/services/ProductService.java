package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.entities.*;
import com.dealharbor.dealharbor_backend.enums.ProductStatus;
import com.dealharbor.dealharbor_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, Authentication authentication) {
        User seller = getUserFromAuthentication(authentication);
        
        // Find category or default to "others" if not found
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElse(categoryRepository.findById("others")
                        .orElseThrow(() -> new RuntimeException("Default 'others' category not found. Please run category initialization.")));

        Product product = Product.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .isNegotiable(request.isNegotiable())
                .condition(request.getCondition())
                .brand(request.getBrand())
                .model(request.getModel())
                .category(category)
                .seller(seller) // ✅ SELLER ID IS PROPERLY ATTACHED HERE
                .tags(request.getTags() != null ? String.join(",", request.getTags()) : null)
                .pickupLocation(request.getPickupLocation())
                .deliveryAvailable(request.isDeliveryAvailable())
                .status(ProductStatus.PENDING) // All products start as PENDING for admin approval
                .viewCount(0)
                .favoriteCount(0)
                .isFeatured(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        product = productRepository.save(product);

        // Save images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageUrl(request.getImageUrls().get(i))
                        .isPrimary(i == 0) // First image is primary
                        .sortOrder(i)
                        .createdAt(Instant.now())
                        .build();
                productImageRepository.save(image);
            }
        }

        // Update seller stats
        seller.setTotalListings(seller.getTotalListings() + 1);
        seller.setActiveListings(seller.getActiveListings() + 1);
        userRepository.save(seller);

        return convertToProductResponse(product);
    }

    public PagedResponse<ProductResponse> getAllProducts(int page, int size, String sortBy) {
        Sort sort = createSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // ✅ ONLY SHOW APPROVED PRODUCTS (NOT SOLD ONES)
        Page<Product> productPage = productRepository.findByStatusOrderByCreatedAtDesc(
                ProductStatus.APPROVED, pageable);
        
        return convertToPagedResponse(productPage);
    }

    public PagedResponse<ProductResponse> getProductsByCategory(String categoryId, int page, int size, String sortBy) {
        Sort sort = createSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // ✅ ONLY SHOW APPROVED PRODUCTS (NOT SOLD ONES)
        Page<Product> productPage = productRepository.findByCategoryIdAndStatusOrderByCreatedAtDesc(
                categoryId, ProductStatus.APPROVED, pageable);
        
        return convertToPagedResponse(productPage);
    }

    public PagedResponse<ProductResponse> searchProducts(ProductSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "date_desc";
        
        Sort sort = createSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage;
        
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            // ✅ ONLY SEARCH APPROVED PRODUCTS (NOT SOLD ONES)
            productPage = productRepository.searchByKeyword(
                    request.getKeyword().trim(), ProductStatus.APPROVED, pageable);
        } else {
            // ✅ ONLY FILTER APPROVED PRODUCTS (NOT SOLD ONES)
            productPage = productRepository.findWithFilters(
                    ProductStatus.APPROVED,
                    request.getCategoryId(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    request.getConditions() != null && !request.getConditions().isEmpty() 
                        ? request.getConditions().get(0).name() : null,
                    pageable
            );
        }
        
        // Filter by verified students if requested
        if (request.getVerifiedStudentsOnly() != null && request.getVerifiedStudentsOnly()) {
            List<ProductResponse> filteredContent = convertToPagedResponse(productPage).getContent()
                    .stream()
                    .filter(ProductResponse::isSellerIsVerifiedStudent)
                    .collect(Collectors.toList());
            
            return new PagedResponse<>(
                    filteredContent,
                    page, size,
                    filteredContent.size(),
                    (int) Math.ceil((double) filteredContent.size() / size),
                    page == 0,
                    filteredContent.size() <= size,
                    false, page > 0
            );
        }
        
        return convertToPagedResponse(productPage);
    }

    public PagedResponse<ProductResponse> getFeaturedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // ✅ ONLY SHOW APPROVED AND FEATURED PRODUCTS (NOT SOLD ONES)
        Page<Product> productPage = productRepository.findByStatusAndIsFeaturedTrueOrderByCreatedAtDesc(
                ProductStatus.APPROVED, pageable);
        
        return convertToPagedResponse(productPage);
    }

    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // ✅ ALLOW VIEWING SOLD PRODUCTS BUT SHOW STATUS
        if (product.getStatus() != ProductStatus.APPROVED && product.getStatus() != ProductStatus.SOLD) {
            throw new RuntimeException("Product not available");
        }
        
        // Increment view count only for approved products
        if (product.getStatus() == ProductStatus.APPROVED) {
            product.setViewCount(product.getViewCount() + 1);
            productRepository.save(product);
        }
        
        return convertToProductResponse(product);
    }

    public PagedResponse<ProductResponse> getUserProducts(Authentication authentication, int page, int size) {
        User user = getUserFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // ✅ SHOW ALL USER'S PRODUCTS (INCLUDING SOLD ONES)
        Page<Product> productPage = productRepository.findBySellerIdOrderByCreatedAtDesc(
                user.getId(), pageable);
        
        return convertToPagedResponse(productPage);
    }

    @Transactional
    public ProductResponse updateProduct(String productId, ProductUpdateRequest request, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (!product.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own products");
        }
        
        if (product.getStatus() == ProductStatus.SOLD) {
            throw new RuntimeException("Cannot update sold products");
        }

        // Update fields
        if (request.getTitle() != null) product.setTitle(request.getTitle());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getOriginalPrice() != null) product.setOriginalPrice(request.getOriginalPrice());
        if (request.getIsNegotiable() != null) product.setIsNegotiable(request.getIsNegotiable());
        if (request.getCondition() != null) product.setCondition(request.getCondition());
        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getModel() != null) product.setModel(request.getModel());
        if (request.getPickupLocation() != null) product.setPickupLocation(request.getPickupLocation());
        if (request.getDeliveryAvailable() != null) product.setDeliveryAvailable(request.getDeliveryAvailable());
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElse(categoryRepository.findById("others")
                            .orElseThrow(() -> new RuntimeException("Default 'others' category not found")));
            product.setCategory(category);
        }
        
        if (request.getTags() != null) {
            product.setTags(String.join(",", request.getTags()));
        }
        
        // Update images if provided
        if (request.getImageUrls() != null) {
            productImageRepository.deleteByProductId(productId);
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageUrl(request.getImageUrls().get(i))
                        .isPrimary(i == 0)
                        .sortOrder(i)
                        .createdAt(Instant.now())
                        .build();
                productImageRepository.save(image);
            }
        }
        
        product.setUpdatedAt(Instant.now());
        product.setStatus(ProductStatus.PENDING); // Re-submit for approval
        product = productRepository.save(product);
        
        return convertToProductResponse(product);
    }

    @Transactional
    public void deleteProduct(String productId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (!product.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own products");
        }
        
        if (product.getStatus() == ProductStatus.SOLD) {
            throw new RuntimeException("Cannot delete sold products");
        }
        
        // Delete all product images from S3 and database
        deleteProductImages(product);
        
        // Hard delete the product from database
        productRepository.delete(product);
        
        // Update seller stats
        User seller = product.getSeller();
        seller.setActiveListings(Math.max(0, seller.getActiveListings() - 1));
        userRepository.save(seller);
        
        log.info("Product {} deleted by user {}", productId, user.getEmail());
    }
    
    /**
     * Scheduled task to automatically delete rejected products and products pending for more than 14 days
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void autoDeleteExpiredProducts() {
        log.info("Starting automatic cleanup of expired products...");
        
        Instant fourteenDaysAgo = Instant.now().minus(14, ChronoUnit.DAYS);
        
        // Find all rejected products
        List<Product> rejectedProducts = productRepository.findByStatus(ProductStatus.REJECTED);
        
        // Find all pending products older than 14 days
        List<Product> oldPendingProducts = productRepository.findByStatusAndCreatedAtBefore(
                ProductStatus.PENDING, fourteenDaysAgo);
        
        int deletedCount = 0;
        
        // Delete rejected products
        for (Product product : rejectedProducts) {
            try {
                deleteProductImages(product);
                productRepository.delete(product);
                
                // Update seller stats
                User seller = product.getSeller();
                seller.setActiveListings(Math.max(0, seller.getActiveListings() - 1));
                userRepository.save(seller);
                
                deletedCount++;
                log.info("Auto-deleted rejected product: {} ({})", product.getId(), product.getTitle());
            } catch (Exception e) {
                log.error("Failed to delete rejected product {}: {}", product.getId(), e.getMessage());
            }
        }
        
        // Delete old pending products
        for (Product product : oldPendingProducts) {
            try {
                deleteProductImages(product);
                productRepository.delete(product);
                
                // Update seller stats
                User seller = product.getSeller();
                seller.setActiveListings(Math.max(0, seller.getActiveListings() - 1));
                userRepository.save(seller);
                
                deletedCount++;
                log.info("Auto-deleted old pending product: {} ({}) - Created: {}", 
                        product.getId(), product.getTitle(), product.getCreatedAt());
            } catch (Exception e) {
                log.error("Failed to delete old pending product {}: {}", product.getId(), e.getMessage());
            }
        }
        
        log.info("Automatic cleanup completed. Deleted {} products.", deletedCount);
    }
    
    /**
     * Helper method to delete all images associated with a product
     */
    private void deleteProductImages(Product product) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(product.getId());
        
        for (ProductImage image : images) {
            try {
                // Delete from S3
                boolean deleted = storageService.deleteFile(image.getImageUrl());
                if (deleted) {
                    log.debug("Deleted image from S3: {}", image.getImageUrl());
                } else {
                    log.warn("Failed to delete image from S3: {}", image.getImageUrl());
                }
            } catch (Exception e) {
                log.error("Error deleting image from S3: {}", image.getImageUrl(), e);
            }
        }
        
        // Delete all image records from database
        productImageRepository.deleteByProductId(product.getId());
        log.debug("Deleted {} image records from database for product {}", images.size(), product.getId());
    }

    // Helper methods
    private Sort createSort(String sortBy) {
        return switch (sortBy) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "date_asc" -> Sort.by("createdAt").ascending();
            case "date_desc" -> Sort.by("createdAt").descending();
            case "popularity" -> Sort.by("viewCount").descending();
            default -> Sort.by("createdAt").descending();
        };
    }

    private PagedResponse<ProductResponse> convertToPagedResponse(Page<Product> productPage) {
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
                product.getSeller().getId(), // ✅ SELLER ID IS INCLUDED
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

    private User getUserFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
