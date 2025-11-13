package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.SoldProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SoldProductRepository extends JpaRepository<SoldProduct, String> {
    
    // Find by seller
    Page<SoldProduct> findBySellerIdOrderBySoldAtDesc(String sellerId, Pageable pageable);
    
    List<SoldProduct> findBySellerIdOrderBySoldAtDesc(String sellerId);
    
    // Find by buyer
    Page<SoldProduct> findByBuyerIdOrderBySoldAtDesc(String buyerId, Pageable pageable);
    
    // Count sold products by seller
    long countBySellerId(String sellerId);
    
    // Count sold products by buyer
    long countByBuyerId(String buyerId);
    
    // Get total revenue for seller
    @Query("SELECT COALESCE(SUM(sp.soldPrice), 0) FROM SoldProduct sp WHERE sp.sellerId = :sellerId")
    Double getTotalRevenueBySellerId(String sellerId);
    
    // Find by date range
    Page<SoldProduct> findBySoldAtBetweenOrderBySoldAtDesc(Instant startDate, Instant endDate, Pageable pageable);
    
    // Find by seller and date range
    Page<SoldProduct> findBySellerIdAndSoldAtBetweenOrderBySoldAtDesc(
            String sellerId, Instant startDate, Instant endDate, Pageable pageable);
}
