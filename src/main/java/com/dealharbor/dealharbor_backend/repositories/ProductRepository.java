package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.Product;
import com.dealharbor.dealharbor_backend.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    
    // Basic queries
    Page<Product> findByStatusOrderByCreatedAtDesc(ProductStatus status, Pageable pageable);
    Page<Product> findBySellerIdOrderByCreatedAtDesc(String sellerId, Pageable pageable);
    Page<Product> findBySellerIdAndStatusOrderByCreatedAtDesc(String sellerId, ProductStatus status, Pageable pageable);
    Page<Product> findByCategoryIdAndStatusOrderByCreatedAtDesc(String categoryId, ProductStatus status, Pageable pageable);
    
    // Search queries
    @Query("SELECT p FROM Product p WHERE p.status = :status AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.createdAt DESC")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, @Param("status") ProductStatus status, Pageable pageable);
    
    // Admin search (all statuses)
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.seller.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.seller.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.createdAt DESC")
    Page<Product> searchByKeywordForAdmin(@Param("keyword") String keyword, Pageable pageable);
    
    // Filter queries
    @Query("SELECT p FROM Product p WHERE p.status = :status AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:condition IS NULL OR p.condition = :condition) " +
           "ORDER BY p.createdAt DESC")
    Page<Product> findWithFilters(
        @Param("status") ProductStatus status,
        @Param("categoryId") String categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("condition") String condition,
        Pageable pageable
    );
    
    // Featured products
    Page<Product> findByStatusAndIsFeaturedTrueOrderByCreatedAtDesc(ProductStatus status, Pageable pageable);
    
    // Admin queries
    Page<Product> findByStatusOrderByCreatedAtAsc(ProductStatus status, Pageable pageable);
    
    // Statistics
    long countBySellerIdAndStatus(String sellerId, ProductStatus status);
    long countByStatus(ProductStatus status);
    long countByCategoryIdAndStatus(String categoryId, ProductStatus status);
    long countByStatusAndIsFeaturedTrue(ProductStatus status);
    long countByCreatedAtAfter(Instant since);
    long countByCategoryAndStatus(com.dealharbor.dealharbor_backend.entities.Category category, ProductStatus status);
    
    // Category-based queries
    Page<Product> findByCategoryAndStatus(com.dealharbor.dealharbor_backend.entities.Category category, ProductStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller.id = :sellerId")
    long countBySellerId(@Param("sellerId") String sellerId);
    
    // Admin specific queries
    List<Product> findBySellerIdAndStatusNot(String sellerId, ProductStatus status);
    
    // Auto-deletion queries
    List<Product> findByStatus(ProductStatus status);
    List<Product> findByStatusAndCreatedAtBefore(ProductStatus status, Instant createdBefore);
    
    // Homepage queries for landing page
    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' AND p.createdAt >= :since " +
           "ORDER BY (p.viewCount + p.favoriteCount * 2) DESC, p.createdAt DESC")
    Page<Product> findTrendingProducts(@Param("since") Instant since, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' AND " +
           "p.originalPrice > 0 AND " +
           "((p.originalPrice - p.price) / p.originalPrice) >= 0.2 " +
           "ORDER BY ((p.originalPrice - p.price) / p.originalPrice) DESC, p.createdAt DESC")
    Page<Product> findDealsOfTheDay(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' " +
           "ORDER BY p.seller.sellerRating DESC, p.favoriteCount DESC, p.viewCount DESC")
    Page<Product> findTopRatedProducts(Pageable pageable);
    
    @Query("SELECT c.name, COUNT(p) FROM Product p JOIN p.category c " +
           "WHERE p.status = 'APPROVED' GROUP BY c.name ORDER BY COUNT(p) DESC")
    List<Object[]> findMostPopularCategory();
}
