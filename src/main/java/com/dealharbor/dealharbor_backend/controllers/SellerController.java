package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.PagedResponse;
import com.dealharbor.dealharbor_backend.dto.ProductResponse;
import com.dealharbor.dealharbor_backend.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
public class SellerController {
    
    private final ProductService productService;

    /**
     * Get all approved products for a specific seller
     * Public endpoint - no authentication required
     */
    @GetMapping("/{sellerId}/products")
    public ResponseEntity<PagedResponse<ProductResponse>> getSellerProducts(
            @PathVariable String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getProductsBySeller(sellerId, page, size));
    }
}
