package com.dealharbor.dealharbor_backend.entities;

import com.dealharbor.dealharbor_backend.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(length = 500)
    private String actionUrl; // URL to navigate when clicked

    @Column(length = 100)
    private String relatedEntityId; // ID of related product, order, etc.

    @Column(length = 50)
    private String relatedEntityType; // PRODUCT, ORDER, USER, etc.

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isEmailSent = false;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
