package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.UnsoldProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UnsoldProductRepository extends JpaRepository<UnsoldProduct, String> {
    
    // Find by seller
    Page<UnsoldProduct> findBySellerIdOrderByArchivedAtDesc(String sellerId, Pageable pageable);
    
    List<UnsoldProduct> findBySellerIdOrderByArchivedAtDesc(String sellerId);
    
    // Count unsold products by seller
    long countBySellerId(String sellerId);
    
    // Find by date range
    Page<UnsoldProduct> findByArchivedAtBetweenOrderByArchivedAtDesc(
            Instant startDate, Instant endDate, Pageable pageable);
    
    // Find by seller and date range
    Page<UnsoldProduct> findBySellerIdAndArchivedAtBetweenOrderByArchivedAtDesc(
            String sellerId, Instant startDate, Instant endDate, Pageable pageable);
}
