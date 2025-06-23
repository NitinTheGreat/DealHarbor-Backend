package com.dealharbor.dealharbor_backend.entities;

import com.dealharbor.dealharbor_backend.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean enabled;

    private boolean locked;

    private Instant createdAt;
}
