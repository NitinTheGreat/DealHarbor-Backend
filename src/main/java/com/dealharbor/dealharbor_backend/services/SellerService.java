package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.TopSellerResponse;
import com.dealharbor.dealharbor_backend.entities.User;
import com.dealharbor.dealharbor_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerService {
    
    private final UserRepository userRepository;

    /**
     * Get top-rated sellers for homepage spotlight
     */
    public List<TopSellerResponse> getTopRatedSellers(int limit, BigDecimal minRating, boolean verifiedOnly) {
        List<User> sellers = userRepository.findActiveSellers();
        
        return sellers.stream()
                .filter(user -> user.getTotalSales() > 0) // Only sellers with at least 1 sale
                .filter(user -> !verifiedOnly || user.isVerifiedStudent())
                .filter(user -> user.getSellerRating().compareTo(minRating) >= 0)
                .sorted(Comparator
                        .comparing(User::getSellerRating).reversed()
                        .thenComparing(Comparator.comparing(User::getTotalSales).reversed()))
                .limit(limit)
                .map(this::convertToTopSellerResponse)
                .collect(Collectors.toList());
    }

    private TopSellerResponse convertToTopSellerResponse(User user) {
        return TopSellerResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .rating(user.getSellerRating())
                .reviewCount(user.getPositiveReviews() + user.getNegativeReviews())
                .totalSales(user.getTotalSales())
                .isVerified(user.isVerifiedStudent())
                .badge(user.getSellerBadge() != null ? user.getSellerBadge().name() : "NEW_SELLER")
                .joinedAt(user.getCreatedAt())
                .build();
    }
}
