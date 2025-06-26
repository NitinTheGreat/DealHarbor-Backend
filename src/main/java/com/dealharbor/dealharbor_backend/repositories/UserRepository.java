package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUniversityEmailAndIsVerifiedStudentTrue(String universityEmail);
    Optional<User> findByGoogleId(String googleId);
    Optional<User> findByGithubId(String githubId);
    
    // Admin queries
    Page<User> findByIsBannedTrue(Pageable pageable);
    Page<User> findByIsVerifiedStudentTrue(Pageable pageable);
    Page<User> findByDeletedTrue(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.universityId) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY u.createdAt DESC")
    Page<User> searchByKeywordForAdmin(@Param("keyword") String keyword, Pageable pageable);
    
    // Statistics
    long countByDeletedFalseAndEnabledTrue();
    long countByIsBannedTrue();
    long countByIsVerifiedStudentTrue();
    long countByCreatedAtAfter(Instant since);
}
