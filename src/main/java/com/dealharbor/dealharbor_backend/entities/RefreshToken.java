package com.dealharbor.dealharbor_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Instant expiresAt;
}
