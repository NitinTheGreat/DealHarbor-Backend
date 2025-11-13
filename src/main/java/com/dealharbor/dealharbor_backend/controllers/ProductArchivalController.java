package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.entities.SoldProduct;
import com.dealharbor.dealharbor_backend.entities.UnsoldProduct;
import com.dealharbor.dealharbor_backend.entities.User;
import com.dealharbor.dealharbor_backend.repositories.SoldProductRepository;
import com.dealharbor.dealharbor_backend.repositories.UnsoldProductRepository;
import com.dealharbor.dealharbor_backend.services.ProductArchivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/archived")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
public class ProductArchivalController {

    private final ProductArchivalService productArchivalService;
    private final SoldProductRepository soldProductRepository;
    private final UnsoldProductRepository unsoldProductRepository;

    /**
     * Mark a product as sold
     * POST /api/products/archived/mark-sold/{productId}
     */
    @PostMapping("/mark-sold/{productId}")
    public ResponseEntity<?> markProductAsSold(
            @PathVariable String productId,
            @RequestBody(required = false) MarkSoldRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            String buyerId = request != null ? request.buyerId() : null;
            Double soldPrice = request != null ? request.soldPrice() : null;
            
            SoldProduct soldProduct = productArchivalService.markProductAsSold(
                    productId, currentUser.getId(), buyerId, soldPrice);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Product marked as sold successfully",
                    "soldProduct", convertToSoldProductResponse(soldProduct)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get user's sold products
     * GET /api/products/archived/sold?page=0&size=20
     */
    @GetMapping("/sold")
    public ResponseEntity<Page<SoldProductResponse>> getSoldProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<SoldProduct> soldProducts = soldProductRepository.findBySellerIdOrderBySoldAtDesc(
                currentUser.getId(), pageable);
        
        return ResponseEntity.ok(soldProducts.map(this::convertToSoldProductResponse));
    }

    /**
     * Get all user's sold products (no pagination)
     * GET /api/products/archived/sold/all
     */
    @GetMapping("/sold/all")
    public ResponseEntity<List<SoldProductResponse>> getAllSoldProducts(
            @AuthenticationPrincipal User currentUser) {
        
        List<SoldProduct> soldProducts = soldProductRepository.findBySellerIdOrderBySoldAtDesc(
                currentUser.getId());
        
        return ResponseEntity.ok(soldProducts.stream()
                .map(this::convertToSoldProductResponse)
                .toList());
    }

    /**
     * Get user's unsold (expired) products
     * GET /api/products/archived/unsold?page=0&size=20
     */
    @GetMapping("/unsold")
    public ResponseEntity<Page<UnsoldProductResponse>> getUnsoldProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UnsoldProduct> unsoldProducts = unsoldProductRepository.findBySellerIdOrderByArchivedAtDesc(
                currentUser.getId(), pageable);
        
        return ResponseEntity.ok(unsoldProducts.map(this::convertToUnsoldProductResponse));
    }

    /**
     * Get all user's unsold products (no pagination)
     * GET /api/products/archived/unsold/all
     */
    @GetMapping("/unsold/all")
    public ResponseEntity<List<UnsoldProductResponse>> getAllUnsoldProducts(
            @AuthenticationPrincipal User currentUser) {
        
        List<UnsoldProduct> unsoldProducts = unsoldProductRepository.findBySellerIdOrderByArchivedAtDesc(
                currentUser.getId());
        
        return ResponseEntity.ok(unsoldProducts.stream()
                .map(this::convertToUnsoldProductResponse)
                .toList());
    }

    /**
     * Get archival statistics
     * GET /api/products/archived/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ArchivalStatsResponse> getArchivalStats(
            @AuthenticationPrincipal User currentUser) {
        
        ProductArchivalService.ArchivalStats stats = 
                productArchivalService.getArchivalStats(currentUser.getId());
        
        return ResponseEntity.ok(new ArchivalStatsResponse(
                stats.soldCount(),
                stats.unsoldCount(),
                stats.totalRevenue()
        ));
    }

    /**
     * Get specific sold product details
     * GET /api/products/archived/sold/{productId}
     */
    @GetMapping("/sold/{productId}")
    public ResponseEntity<?> getSoldProductById(
            @PathVariable String productId,
            @AuthenticationPrincipal User currentUser) {
        
        return soldProductRepository.findById(productId)
                .filter(sp -> sp.getSellerId().equals(currentUser.getId()))
                .map(sp -> ResponseEntity.ok(convertToSoldProductResponse(sp)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get specific unsold product details
     * GET /api/products/archived/unsold/{productId}
     */
    @GetMapping("/unsold/{productId}")
    public ResponseEntity<?> getUnsoldProductById(
            @PathVariable String productId,
            @AuthenticationPrincipal User currentUser) {
        
        return unsoldProductRepository.findById(productId)
                .filter(up -> up.getSellerId().equals(currentUser.getId()))
                .map(up -> ResponseEntity.ok(convertToUnsoldProductResponse(up)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ============= DTOs =============

    private record MarkSoldRequest(String buyerId, Double soldPrice) {}

    private record ArchivalStatsResponse(
            long totalSold,
            long totalUnsold,
            double totalRevenue
    ) {}

    private SoldProductResponse convertToSoldProductResponse(SoldProduct sp) {
        return new SoldProductResponse(
                sp.getId(),
                sp.getTitle(),
                sp.getDescription(),
                sp.getPrice(),
                sp.getOriginalPrice(),
                sp.getSoldPrice(),
                sp.getCondition().name(),
                sp.getBrand(),
                sp.getModel(),
                sp.getCategoryId(),
                sp.getCategoryName(),
                sp.getSellerId(),
                sp.getSellerName(),
                sp.getBuyerId(),
                sp.getBuyerName(),
                sp.getPrimaryImageUrl(),
                sp.getImageUrls(),
                sp.getTags(),
                sp.getViewCount(),
                sp.getFavoriteCount(),
                sp.getPickupLocation(),
                sp.getCreatedAt().toString(),
                sp.getSoldAt().toString(),
                sp.getArchivedAt().toString()
        );
    }

    private UnsoldProductResponse convertToUnsoldProductResponse(UnsoldProduct up) {
        return new UnsoldProductResponse(
                up.getId(),
                up.getTitle(),
                up.getDescription(),
                up.getPrice(),
                up.getOriginalPrice(),
                up.getCondition().name(),
                up.getBrand(),
                up.getModel(),
                up.getCategoryId(),
                up.getCategoryName(),
                up.getSellerId(),
                up.getSellerName(),
                up.getPrimaryImageUrl(),
                up.getImageUrls(),
                up.getTags(),
                up.getViewCount(),
                up.getFavoriteCount(),
                up.getPickupLocation(),
                up.getCreatedAt().toString(),
                up.getExpiredAt().toString(),
                up.getArchivedAt().toString(),
                up.getArchivalReason()
        );
    }

    private record SoldProductResponse(
            String id,
            String title,
            String description,
            java.math.BigDecimal price,
            java.math.BigDecimal originalPrice,
            java.math.BigDecimal soldPrice,
            String condition,
            String brand,
            String model,
            String categoryId,
            String categoryName,
            String sellerId,
            String sellerName,
            String buyerId,
            String buyerName,
            String primaryImageUrl,
            String imageUrls,
            String tags,
            Integer viewCount,
            Integer favoriteCount,
            String pickupLocation,
            String createdAt,
            String soldAt,
            String archivedAt
    ) {}

    private record UnsoldProductResponse(
            String id,
            String title,
            String description,
            java.math.BigDecimal price,
            java.math.BigDecimal originalPrice,
            String condition,
            String brand,
            String model,
            String categoryId,
            String categoryName,
            String sellerId,
            String sellerName,
            String primaryImageUrl,
            String imageUrls,
            String tags,
            Integer viewCount,
            Integer favoriteCount,
            String pickupLocation,
            String createdAt,
            String expiredAt,
            String archivedAt,
            String archivalReason
    ) {}
}
