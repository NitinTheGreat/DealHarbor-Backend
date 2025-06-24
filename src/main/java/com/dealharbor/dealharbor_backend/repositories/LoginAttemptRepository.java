package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {
    @Query("SELECT COUNT(l) FROM LoginAttempt l WHERE l.email = :email AND l.successful = false AND l.attemptTime > :since")
    int countFailedAttemptsSince(@Param("email") String email, @Param("since") Instant since);
    
    List<LoginAttempt> findByEmailOrderByAttemptTimeDesc(String email);
    
    @Query("SELECT l FROM LoginAttempt l WHERE l.email = :email AND l.attemptTime > :since ORDER BY l.attemptTime DESC")
    List<LoginAttempt> findRecentAttempts(@Param("email") String email, @Param("since") Instant since);
}
