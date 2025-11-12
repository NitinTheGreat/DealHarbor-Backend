package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
public class ProductController {
    
    private final ProductService productService;

    // Public endpoints
    
    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date_desc") String sortBy) {
        return ResponseEntity.ok(productService.getAllProducts(page, size, sortBy));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PagedResponse<ProductResponse>> getProductsByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date_desc") String sortBy) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, page, size, sortBy));
    }

    @PostMapping("/search")
    public ResponseEntity<PagedResponse<ProductResponse>> searchProducts(@RequestBody ProductSearchRequest request) {
        return ResponseEntity.ok(productService.searchProducts(request));
    }

    @GetMapping("/featured")
    public ResponseEntity<PagedResponse<ProductResponse>> getFeaturedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getFeaturedProducts(page, size));
    }

    @GetMapping("/trending")
    public ResponseEntity<PagedResponse<ProductResponse>> getTrendingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(productService.getTrendingProducts(page, size));
    }

    @GetMapping("/recent")
    public ResponseEntity<PagedResponse<ProductResponse>> getRecentProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(productService.getRecentProducts(page, size));
    }

    @GetMapping("/deals")
    public ResponseEntity<PagedResponse<ProductResponse>> getDealsOfTheDay(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(productService.getDealsOfTheDay(page, size));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<PagedResponse<ProductResponse>> getTopRatedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(productService.getTopRatedProducts(page, size));
    }

    @GetMapping("/by-category-preview")
    public ResponseEntity<List<CategoryProductPreview>> getProductsByCategoryPreview(
            @RequestParam(defaultValue = "6") int productsPerCategory) {
        return ResponseEntity.ok(productService.getProductsByCategoryPreview(productsPerCategory));
    }

    @GetMapping("/homepage-stats")
    public ResponseEntity<HomepageStatsResponse> getHomepageStats() {
        return ResponseEntity.ok(productService.getHomepageStats());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    // Protected endpoints (authentication required)

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody ProductCreateRequest request, 
            Authentication authentication) {
        return ResponseEntity.ok(productService.createProduct(request, authentication));
    }

    @GetMapping("/my-products")
    public ResponseEntity<PagedResponse<ProductResponse>> getUserProducts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getUserProducts(authentication, page, size));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String productId,
            @RequestBody ProductUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(productService.updateProduct(productId, request, authentication));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable String productId,
            Authentication authentication) {
        productService.deleteProduct(productId, authentication);
        return ResponseEntity.ok("Product deleted successfully");
    }
}
