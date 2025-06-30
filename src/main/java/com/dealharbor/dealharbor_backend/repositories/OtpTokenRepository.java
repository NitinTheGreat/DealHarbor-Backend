package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, String> {
    
    Optional<OtpToken> findByEmailAndOtp(String email, String otp);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpToken ot WHERE ot.email = :email")
    void deleteByEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(ot) FROM OtpToken ot WHERE ot.email = :email")
    long countByEmail(@Param("email") String email);
}
