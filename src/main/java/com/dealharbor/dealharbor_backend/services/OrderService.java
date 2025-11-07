package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.entities.*;
import com.dealharbor.dealharbor_backend.enums.OrderStatus;
import com.dealharbor.dealharbor_backend.enums.ProductStatus;
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

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, Authentication authentication) {
        User buyer = getUserFromAuthentication(authentication);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.getStatus() != ProductStatus.APPROVED) {
            throw new RuntimeException("Product is not available for purchase");
        }
        
        if (product.getSeller().getId().equals(buyer.getId())) {
            throw new RuntimeException("Cannot buy your own product");
        }
        
        // Check if there's already a pending order for this product
        if (orderRepository.findByProductIdAndStatus(request.getProductId(), OrderStatus.PENDING).isPresent()) {
            throw new RuntimeException("There is already a pending order for this product");
        }
        
        Order order = Order.builder()
                .buyer(buyer)
                .seller(product.getSeller())
                .product(product)
                .productTitle(product.getTitle())
                .agreedPrice(request.getAgreedPrice())
                .originalPrice(product.getPrice())
                .status(OrderStatus.PENDING)
                .buyerNotes(request.getBuyerNotes())
                .pickupLocation(request.getPickupLocation())
                .deliveryMethod(request.getDeliveryMethod())
                .build();
        
        order = orderRepository.save(order);
        
        // Create notification for seller
        notificationService.createNotification(
                product.getSeller().getId(),
                "New Order Received",
                buyer.getName() + " wants to buy your product '" + product.getTitle() + "'",
                NotificationType.ORDER_CREATED,
                "/orders/" + order.getId(),
                order.getId(),
                "ORDER"
        );
        
        return convertToOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(String orderId, OrderUpdateRequest request, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Only seller can update order status
        if (!order.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("Only the seller can update order status");
        }
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());
        
        if (request.getSellerNotes() != null) {
            order.setSellerNotes(request.getSellerNotes());
        }
        
        if (request.getPickupLocation() != null) {
            order.setPickupLocation(request.getPickupLocation());
        }
        
        // Update timestamps based on status
        switch (request.getStatus()) {
            case CONFIRMED -> {
                if (oldStatus == OrderStatus.PENDING) {
                    order.setConfirmedAt(Instant.now());
                }
            }
            case COMPLETED -> {
                if (oldStatus != OrderStatus.COMPLETED) {
                    order.setCompletedAt(Instant.now());
                    // Update product status to sold
                    Product product = order.getProduct();
                    product.setStatus(ProductStatus.SOLD);
                    product.setSoldAt(Instant.now());
                    productRepository.save(product);
                    
                    // Update seller stats
                    User seller = order.getSeller();
                    seller.setTotalSales(seller.getTotalSales() + 1);
                    seller.setTotalRevenue(seller.getTotalRevenue().add(order.getAgreedPrice()));
                    seller.setActiveListings(Math.max(0, seller.getActiveListings() - 1));
                    if (seller.getFirstSaleAt() == null) {
                        seller.setFirstSaleAt(Instant.now());
                    }
                    userRepository.save(seller);
                    
                    // Update buyer stats
                    User buyer = order.getBuyer();
                    buyer.setTotalPurchases(buyer.getTotalPurchases() + 1);
                    userRepository.save(buyer);
                }
            }
            case CANCELLED -> {
                if (oldStatus != OrderStatus.CANCELLED) {
                    order.setCancelledAt(Instant.now());
                }
            }
        }
        
        order = orderRepository.save(order);
        
        // Notify buyer about status change
        String statusMessage = getStatusChangeMessage(request.getStatus());
        notificationService.createNotification(
                order.getBuyer().getId(),
                "Order Status Updated",
                "Your order for '" + order.getProductTitle() + "' has been " + statusMessage,
                getNotificationTypeForStatus(request.getStatus()),
                "/orders/" + order.getId(),
                order.getId(),
                "ORDER"
        );
        
        return convertToOrderResponse(order);
    }

    public PagedResponse<OrderResponse> getBuyerOrders(Authentication authentication, OrderStatus status, int page, int size) {
        User buyer = getUserFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Order> orderPage;
        if (status != null) {
            orderPage = orderRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(buyer.getId(), status, pageable);
        } else {
            orderPage = orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyer.getId(), pageable);
        }
        
        return convertToPagedOrderResponse(orderPage);
    }

    public PagedResponse<OrderResponse> getSellerOrders(Authentication authentication, OrderStatus status, int page, int size) {
        User seller = getUserFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Order> orderPage;
        if (status != null) {
            orderPage = orderRepository.findBySellerIdAndStatusOrderByCreatedAtDesc(seller.getId(), status, pageable);
        } else {
            orderPage = orderRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId(), pageable);
        }
        
        return convertToPagedOrderResponse(orderPage);
    }

    public OrderResponse getOrderById(String orderId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Verify user is buyer or seller
        if (!order.getBuyer().getId().equals(user.getId()) && 
            !order.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied to this order");
        }
        
        return convertToOrderResponse(order);
    }

    @Transactional
    public void cancelOrder(String orderId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Only buyer can cancel pending orders, seller can cancel confirmed orders
        if (order.getStatus() == OrderStatus.PENDING && !order.getBuyer().getId().equals(user.getId())) {
            throw new RuntimeException("Only buyer can cancel pending orders");
        }
        
        if (order.getStatus() == OrderStatus.CONFIRMED && !order.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("Only seller can cancel confirmed orders");
        }
        
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel completed orders");
        }
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        orderRepository.save(order);
        
        // Notify the other party
        String recipientId = order.getBuyer().getId().equals(user.getId()) 
                ? order.getSeller().getId() 
                : order.getBuyer().getId();
        
        notificationService.createNotification(
                recipientId,
                "Order Cancelled",
                user.getName() + " cancelled the order for '" + order.getProductTitle() + "'",
                NotificationType.ORDER_CANCELLED,
                "/orders/" + order.getId(),
                order.getId(),
                "ORDER"
        );
    }

    private PagedResponse<OrderResponse> convertToPagedOrderResponse(Page<Order> orderPage) {
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isFirst(),
                orderPage.isLast(),
                orderPage.hasNext(),
                orderPage.hasPrevious()
        );
    }

    private OrderResponse convertToOrderResponse(Order order) {
        String productImageUrl = order.getProduct().getPrimaryImage() != null 
                ? order.getProduct().getPrimaryImage().getImageUrl() 
                : null;
        
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getAgreedPrice(),
                order.getOriginalPrice(),
                order.getBuyerNotes(),
                order.getSellerNotes(),
                order.getPickupLocation(),
                order.getDeliveryMethod(),
                order.getCreatedAt(),
                order.getConfirmedAt(),
                order.getCompletedAt(),
                order.getProduct().getId(),
                order.getProductTitle(),
                productImageUrl,
                order.getBuyer().getId(),
                order.getBuyer().getName(),
                order.getSeller().getId(),
                order.getSeller().getName()
        );
    }

    private String getStatusChangeMessage(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "confirmed";
            case COMPLETED -> "completed";
            case CANCELLED -> "cancelled";
            default -> "updated";
        };
    }

    private NotificationType getNotificationTypeForStatus(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> NotificationType.ORDER_CONFIRMED;
            case COMPLETED -> NotificationType.ORDER_COMPLETED;
            case CANCELLED -> NotificationType.ORDER_CANCELLED;
            default -> NotificationType.ORDER_CREATED;
        };
    }

    private User getUserFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
