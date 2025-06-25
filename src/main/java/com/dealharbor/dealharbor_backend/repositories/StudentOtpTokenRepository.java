package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.StudentOtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StudentOtpTokenRepository extends JpaRepository<StudentOtpToken, String> {
    Optional<StudentOtpToken> findByUserIdAndStudentEmailAndOtp(String userId, String studentEmail, String otp);
    
    @Modifying
    @Query("DELETE FROM StudentOtpToken s WHERE s.userId = :userId")
    void deleteByUserId(String userId);
    
    @Modifying
    @Query("DELETE FROM StudentOtpToken s WHERE s.studentEmail = :studentEmail")
    void deleteByStudentEmail(String studentEmail);
}
