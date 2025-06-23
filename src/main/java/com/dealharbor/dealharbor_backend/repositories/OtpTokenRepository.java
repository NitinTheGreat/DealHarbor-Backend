package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, String> {
    Optional<OtpToken> findByEmailAndOtp(String email, String otp);
    void deleteByEmail(String email);
}
