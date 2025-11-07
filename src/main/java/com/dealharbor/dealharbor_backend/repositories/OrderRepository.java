package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.Order;
import com.dealharbor.dealharbor_backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    
    // Buyer queries
    Page<Order> findByBuyerIdOrderByCreatedAtDesc(String buyerId, Pageable pageable);
    Page<Order> findByBuyerIdAndStatusOrderByCreatedAtDesc(String buyerId, OrderStatus status, Pageable pageable);
    
    // Seller queries
    Page<Order> findBySellerIdOrderByCreatedAtDesc(String sellerId, Pageable pageable);
    Page<Order> findBySellerIdAndStatusOrderByCreatedAtDesc(String sellerId, OrderStatus status, Pageable pageable);
    
    // Product queries
    List<Order> findByProductIdOrderByCreatedAtDesc(String productId);
    Optional<Order> findByProductIdAndStatus(String productId, OrderStatus status);
    
    // Order number lookup
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Statistics
    long countBySellerIdAndStatus(String sellerId, OrderStatus status);
    long countByBuyerIdAndStatus(String buyerId, OrderStatus status);
    long countByStatus(OrderStatus status);
    long countByCreatedAtAfter(Instant since);
}
