package com.dealharbor.dealharbor_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "security_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String eventType; // LOGIN, LOGOUT, PASSWORD_CHANGE, etc.

    @Column(nullable = false)
    private String ipAddress;

    private String userAgent;
    private String description;

    @Column(nullable = false)
    private Instant timestamp;
}
