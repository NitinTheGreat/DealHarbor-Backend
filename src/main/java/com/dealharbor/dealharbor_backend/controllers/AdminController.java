package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.enums.ProductStatus;
import com.dealharbor.dealharbor_backend.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
@PreAuthorize("hasRole('ADMIN')") // ✅ ONLY ADMINS CAN ACCESS
public class AdminController {
    
    private final AdminService adminService;

    // ✅ ADMIN DASHBOARD
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ✅ ADMIN PRODUCT MANAGEMENT
    @GetMapping("/products")
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProducts(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date_desc") String sortBy) {
        return ResponseEntity.ok(adminService.getAllProductsForAdmin(status, page, size, sortBy));
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String productId,
            @RequestBody AdminProductActionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(adminService.adminUpdateProduct(productId, request, authentication));
    }

    @GetMapping("/products/search")
    public ResponseEntity<PagedResponse<ProductResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.searchProductsForAdmin(keyword, page, size));
    }

    // ✅ ADMIN USER MANAGEMENT
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<UserProfileResponse>> getAllUsers(
            @RequestParam(defaultValue = "all") String filter, // all, banned, verified, deleted
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllUsersForAdmin(filter, page, size));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserProfileResponse> updateUser(
            @PathVariable String userId,
            @RequestBody AdminUserActionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(adminService.adminUpdateUser(userId, request, authentication));
    }

    @GetMapping("/users/search")
    public ResponseEntity<PagedResponse<UserProfileResponse>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.searchUsersForAdmin(keyword, page, size));
    }
}
