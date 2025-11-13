package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.entities.*;
import com.dealharbor.dealharbor_backend.enums.ProductStatus;
import com.dealharbor.dealharbor_backend.repositories.ProductRepository;
import com.dealharbor.dealharbor_backend.repositories.SoldProductRepository;
import com.dealharbor.dealharbor_backend.repositories.UnsoldProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductArchivalService {

    private final ProductRepository productRepository;
    private final SoldProductRepository soldProductRepository;
    private final UnsoldProductRepository unsoldProductRepository;
    private final ObjectMapper objectMapper;

    /**
     * Mark a product as sold and move it to sold_products table
     */
    @Transactional
    public SoldProduct markProductAsSold(String productId, String currentUserId, String buyerId, Double soldPrice) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Verify ownership
        if (!product.getSeller().getId().equals(currentUserId)) {
            throw new RuntimeException("You can only mark your own products as sold");
        }

        // Verify product is approved and not already sold
        if (product.getStatus() != ProductStatus.APPROVED) {
            throw new RuntimeException("Only approved products can be marked as sold");
        }

        // Create sold product record
        SoldProduct soldProduct = convertToSoldProduct(product, buyerId, soldPrice);
        
        // Save to sold_products table
        soldProduct = soldProductRepository.save(soldProduct);
        
        // Delete from products table
        productRepository.delete(product);
        
        log.info("Product {} marked as sold and archived", productId);
        
        return soldProduct;
    }

    /**
     * Scheduled task to archive products older than 6 months
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *") // Every day at 2 AM
    @Transactional
    public void archiveExpiredProducts() {
        log.info("Starting scheduled archival of expired products...");
        
        Instant sixMonthsAgo = Instant.now().minus(180, ChronoUnit.DAYS);
        
        // Find approved products older than 6 months
        List<Product> expiredProducts = productRepository.findByStatusAndCreatedAtBefore(
                ProductStatus.APPROVED, sixMonthsAgo);
        
        log.info("Found {} products to archive", expiredProducts.size());
        
        for (Product product : expiredProducts) {
            try {
                archiveAsUnsold(product);
            } catch (Exception e) {
                log.error("Failed to archive product {}: {}", product.getId(), e.getMessage());
            }
        }
        
        log.info("Completed scheduled archival of expired products");
    }

    /**
     * Manually archive a product as unsold
     */
    @Transactional
    public UnsoldProduct archiveAsUnsold(Product product) {
        UnsoldProduct unsoldProduct = convertToUnsoldProduct(product);
        
        // Save to unsold_products table
        unsoldProduct = unsoldProductRepository.save(unsoldProduct);
        
        // Delete from products table
        productRepository.delete(product);
        
        log.info("Product {} archived as unsold", product.getId());
        
        return unsoldProduct;
    }

    /**
     * Convert Product to SoldProduct
     */
    private SoldProduct convertToSoldProduct(Product product, String buyerId, Double soldPrice) {
        User buyer = null;
        if (buyerId != null) {
            // In a real scenario, fetch buyer from UserRepository
            // For now, we'll store just the ID
        }

        // Convert image list to JSON
        String imageUrls = null;
        String primaryImageUrl = null;
        
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<String> urls = product.getImages().stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList());
            try {
                imageUrls = objectMapper.writeValueAsString(urls);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize image URLs", e);
            }
            
            ProductImage primary = product.getPrimaryImage();
            if (primary != null) {
                primaryImageUrl = primary.getImageUrl();
            }
        }

        return SoldProduct.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .soldPrice(soldPrice != null ? 
                        java.math.BigDecimal.valueOf(soldPrice) : product.getPrice())
                .isNegotiable(product.getIsNegotiable())
                .condition(product.getCondition())
                .brand(product.getBrand())
                .model(product.getModel())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getName())
                .buyerId(buyerId)
                .buyerName(buyer != null ? buyer.getName() : null)
                .imageUrls(imageUrls)
                .primaryImageUrl(primaryImageUrl)
                .tags(product.getTags())
                .viewCount(product.getViewCount())
                .favoriteCount(product.getFavoriteCount())
                .pickupLocation(product.getPickupLocation())
                .deliveryAvailable(product.getDeliveryAvailable())
                .createdAt(product.getCreatedAt())
                .soldAt(Instant.now())
                .build();
    }

    /**
     * Convert Product to UnsoldProduct
     */
    private UnsoldProduct convertToUnsoldProduct(Product product) {
        // Convert image list to JSON
        String imageUrls = null;
        String primaryImageUrl = null;
        
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<String> urls = product.getImages().stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList());
            try {
                imageUrls = objectMapper.writeValueAsString(urls);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize image URLs", e);
            }
            
            ProductImage primary = product.getPrimaryImage();
            if (primary != null) {
                primaryImageUrl = primary.getImageUrl();
            }
        }

        return UnsoldProduct.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .isNegotiable(product.getIsNegotiable())
                .condition(product.getCondition())
                .brand(product.getBrand())
                .model(product.getModel())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getName())
                .imageUrls(imageUrls)
                .primaryImageUrl(primaryImageUrl)
                .tags(product.getTags())
                .viewCount(product.getViewCount())
                .favoriteCount(product.getFavoriteCount())
                .pickupLocation(product.getPickupLocation())
                .deliveryAvailable(product.getDeliveryAvailable())
                .createdAt(product.getCreatedAt())
                .expiredAt(product.getCreatedAt().plus(180, ChronoUnit.DAYS))
                .archivalReason("Product expired after 6 months of inactivity")
                .build();
    }

    /**
     * Get statistics about archived products
     */
    public ArchivalStats getArchivalStats(String userId) {
        long soldCount = soldProductRepository.countBySellerId(userId);
        long unsoldCount = unsoldProductRepository.countBySellerId(userId);
        Double totalRevenue = soldProductRepository.getTotalRevenueBySellerId(userId);
        
        return new ArchivalStats(soldCount, unsoldCount, totalRevenue != null ? totalRevenue : 0.0);
    }

    /**
     * Inner class for statistics
     */
    public record ArchivalStats(long soldCount, long unsoldCount, double totalRevenue) {}
}
