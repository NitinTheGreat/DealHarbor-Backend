package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.enums.OrderStatus;
import com.dealharbor.dealharbor_backend.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {
    
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody OrderCreateRequest request, 
            Authentication authentication) {
        return ResponseEntity.ok(orderService.createOrder(request, authentication));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody OrderUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request, authentication));
    }

    @GetMapping("/buyer")
    public ResponseEntity<PagedResponse<OrderResponse>> getBuyerOrders(
            Authentication authentication,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getBuyerOrders(authentication, status, page, size));
    }

    @GetMapping("/seller")
    public ResponseEntity<PagedResponse<OrderResponse>> getSellerOrders(
            Authentication authentication,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getSellerOrders(authentication, status, page, size));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable String orderId,
            Authentication authentication) {
        return ResponseEntity.ok(orderService.getOrderById(orderId, authentication));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> cancelOrder(
            @PathVariable String orderId,
            Authentication authentication) {
        orderService.cancelOrder(orderId, authentication);
        return ResponseEntity.ok("Order cancelled successfully");
    }
}
