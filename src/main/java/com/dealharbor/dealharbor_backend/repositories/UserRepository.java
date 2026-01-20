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
    
    /**
     * Search for sellers (users with products) by name
     * Excludes current user from results
     * Note: User entity has 'name' field (not firstName/lastName) and 'sellerRating' (not rating)
     * Orders by sellerRating and totalListings to show most active sellers first
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "WHERE u.id != :currentUserId " +
           "AND u.deleted = false " +
           "AND u.enabled = true " +
           "AND u.isBanned = false " +
           "AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY u.sellerRating DESC, u.totalListings DESC, u.createdAt DESC")
    Page<User> searchSellers(@Param("query") String query, 
                             @Param("currentUserId") String currentUserId, 
                             Pageable pageable);
    
    // Statistics
    long countByDeletedFalseAndEnabledTrue();
    long countByIsBannedTrue();
    long countByIsVerifiedStudentTrue();
    long countByCreatedAtAfter(Instant since);
    
    // Seller queries for homepage
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.enabled = true AND u.isBanned = false AND u.totalSales > 0 ORDER BY u.sellerRating DESC, u.totalSales DESC")
    java.util.List<User> findActiveSellers();
}
