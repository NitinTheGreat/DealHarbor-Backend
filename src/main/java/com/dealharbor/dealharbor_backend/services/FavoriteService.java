package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.FavoriteResponse;
import com.dealharbor.dealharbor_backend.dto.PagedResponse;
import com.dealharbor.dealharbor_backend.entities.*;
import com.dealharbor.dealharbor_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void addToFavorites(String productId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (favoriteRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new RuntimeException("Product already in favorites");
        }
        
        if (product.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("Cannot add your own product to favorites");
        }
        
        Favorite favorite = Favorite.builder()
                .user(user)
                .product(product)
                .build();
        
        favoriteRepository.save(favorite);
        
        // Update product favorite count
        product.setFavoriteCount(product.getFavoriteCount() + 1);
        productRepository.save(product);
        
        // Notify seller
        notificationService.createNotification(
                product.getSeller().getId(),
                "Product Added to Favorites",
                user.getName() + " added your product '" + product.getTitle() + "' to favorites",
                com.dealharbor.dealharbor_backend.enums.NotificationType.BACK_IN_STOCK,
                "/products/" + productId,
                productId,
                "PRODUCT"
        );
    }

    @Transactional
    public void removeFromFavorites(String productId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        
        if (!favoriteRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new RuntimeException("Product not in favorites");
        }
        
        favoriteRepository.deleteByUserIdAndProductId(user.getId(), productId);
        
        // Update product favorite count
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setFavoriteCount(Math.max(0, product.getFavoriteCount() - 1));
        productRepository.save(product);
    }

    public PagedResponse<FavoriteResponse> getUserFavorites(Authentication authentication, int page, int size) {
        User user = getUserFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Favorite> favoritePage = favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        
        List<FavoriteResponse> content = favoritePage.getContent().stream()
                .map(this::convertToFavoriteResponse)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                favoritePage.getNumber(),
                favoritePage.getSize(),
                favoritePage.getTotalElements(),
                favoritePage.getTotalPages(),
                favoritePage.isFirst(),
                favoritePage.isLast(),
                favoritePage.hasNext(),
                favoritePage.hasPrevious()
        );
    }

    public boolean isProductInFavorites(String productId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        return favoriteRepository.existsByUserIdAndProductId(user.getId(), productId);
    }

    private FavoriteResponse convertToFavoriteResponse(Favorite favorite) {
        Product product = favorite.getProduct();
        String imageUrl = product.getPrimaryImage() != null 
                ? product.getPrimaryImage().getImageUrl() 
                : null;
        
        return new FavoriteResponse(
                favorite.getId(),
                product.getId(),
                product.getTitle(),
                imageUrl,
                product.getPrice().toString(),
                product.getStatus().name(),
                product.getSeller().getId(),
                product.getSeller().getName(),
                favorite.getCreatedAt()
        );
    }

    private User getUserFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
