package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, String> {
    Page<Favorite> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Optional<Favorite> findByUserIdAndProductId(String userId, String productId);
    boolean existsByUserIdAndProductId(String userId, String productId);
    long countByProductId(String productId);
    void deleteByUserIdAndProductId(String userId, String productId);
    
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.product.seller.id = :sellerId")
    long countFavoritesBySellerProducts(String sellerId);
}
