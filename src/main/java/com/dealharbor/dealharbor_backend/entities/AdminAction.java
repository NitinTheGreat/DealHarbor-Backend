package com.dealharbor.dealharbor_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "admin_actions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Column(nullable = false, length = 50)
    private String actionType; // APPROVE_PRODUCT, REJECT_PRODUCT, BAN_USER, etc.

    @Column(nullable = false, length = 50)
    private String targetType; // PRODUCT, USER, ORDER

    @Column(nullable = false)
    private String targetId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "JSON")
    private String details; // Additional action details as JSON

    @Column(length = 45)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
