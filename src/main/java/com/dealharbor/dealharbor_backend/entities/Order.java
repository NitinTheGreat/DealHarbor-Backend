package com.dealharbor.dealharbor_backend.entities;

import com.dealharbor.dealharbor_backend.enums.OrderStatus;
import com.dealharbor.dealharbor_backend.enums.DeliveryMethod;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 20)
    private String orderNumber;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Order details (snapshot at time of order)
    @Column(nullable = false, length = 200)
    private String productTitle;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal agreedPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    // Communication
    @Column(columnDefinition = "TEXT")
    private String buyerNotes;

    @Column(columnDefinition = "TEXT")
    private String sellerNotes;

    // Delivery info
    @Column(length = 200)
    private String pickupLocation;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeliveryMethod deliveryMethod = DeliveryMethod.PICKUP;

    // Timestamps
    @Column(nullable = false)
    private Instant createdAt;

    private Instant confirmedAt;
    private Instant completedAt;
    private Instant cancelledAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        // Generate order number
        if (orderNumber == null) {
            orderNumber = "DH" + System.currentTimeMillis();
        }
    }
}
