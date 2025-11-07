package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.dto.FavoriteResponse;
import com.dealharbor.dealharbor_backend.dto.PagedResponse;
import com.dealharbor.dealharbor_backend.services.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FavoriteController {
    
    private final FavoriteService favoriteService;

    @PostMapping("/{productId}")
    public ResponseEntity<?> addToFavorites(@PathVariable String productId, Authentication authentication) {
        favoriteService.addToFavorites(productId, authentication);
        return ResponseEntity.ok("Product added to favorites");
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromFavorites(@PathVariable String productId, Authentication authentication) {
        favoriteService.removeFromFavorites(productId, authentication);
        return ResponseEntity.ok("Product removed from favorites");
    }

    @GetMapping
    public ResponseEntity<PagedResponse<FavoriteResponse>> getUserFavorites(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(authentication, page, size));
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> isProductInFavorites(@PathVariable String productId, Authentication authentication) {
        return ResponseEntity.ok(favoriteService.isProductInFavorites(productId, authentication));
    }
}
