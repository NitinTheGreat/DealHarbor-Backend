package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    
    List<UserSession> findByUserIdAndActiveTrue(String userId);
    
    Optional<UserSession> findByRefreshToken(String refreshToken);
    
    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.active = false WHERE s.userId = :userId")
    void deactivateAllUserSessions(@Param("userId") String userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    void deleteExpiredSessions(@Param("now") Instant now);
    
    @Query("SELECT COUNT(us) FROM UserSession us WHERE us.userId = :userId AND us.active = true")
    long countActiveByUserId(@Param("userId") String userId);
}
