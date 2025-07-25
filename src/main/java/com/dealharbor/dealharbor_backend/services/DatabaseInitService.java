package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.entities.User;
import com.dealharbor.dealharbor_backend.enums.UserRole;
import com.dealharbor.dealharbor_backend.enums.SellerBadge;
import com.dealharbor.dealharbor_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DatabaseInitService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create a test user if no users exist
        if (userRepository.count() == 0) {
            User testUser = User.builder()
                    .email("test@dealharbor.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .name("Test User")
                    .role(UserRole.USER)
                    .enabled(true)
                    .locked(false)
                    .emailVerified(true)
                    .twoFactorEnabled(false)
                    .failedLoginAttempts(0)
                    .deleted(false)
                    .provider("LOCAL")
                    .profilePhotoUrl("/api/images/default-avatar.png")
                    .sellerRating(BigDecimal.valueOf(4.5))
                    .totalSales(25)
                    .sellerBadge(SellerBadge.TRUSTED_SELLER)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            
            userRepository.save(testUser);
            System.out.println("✅ Test user created: test@dealharbor.com / password123");
            
            // Create admin user
            User adminUser = User.builder()
                    .email("admin@dealharbor.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .name("Admin User")
                    .role(UserRole.ADMIN)
                    .enabled(true)
                    .locked(false)
                    .emailVerified(true)
                    .twoFactorEnabled(false)
                    .failedLoginAttempts(0)
                    .deleted(false)
                    .provider("LOCAL")
                    .profilePhotoUrl("/api/images/default-avatar.png")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            
            userRepository.save(adminUser);
            System.out.println("✅ Admin user created: admin@dealharbor.com / password123");
        }
    }
}
