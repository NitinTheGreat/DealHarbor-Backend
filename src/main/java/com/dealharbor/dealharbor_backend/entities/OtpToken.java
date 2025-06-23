package com.dealharbor.dealharbor_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "otp_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private Instant expiresAt;
}
