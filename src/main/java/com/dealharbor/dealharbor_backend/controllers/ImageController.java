package com.dealharbor.dealharbor_backend.controllers;

import com.dealharbor.dealharbor_backend.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:8080", "http://127.0.0.1:8080"}, allowCredentials = "true")
public class ImageController {

    @Autowired
    private StorageService storageService;

    @GetMapping("/default-avatar.png")
    public ResponseEntity<Resource> getDefaultAvatar() {
        try {
            Resource resource = new ClassPathResource("static/default-avatar.png");
            if (resource.exists()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload-profile-photo")
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please select a file to upload"));
            }

            // Upload to Supabase Storage
            String fileUrl = storageService.uploadProfilePhoto(file);
            
            return ResponseEntity.ok(fileUrl);
            
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to upload file: " + e.getMessage()));
        }
    }

    @PostMapping("/upload-product")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please select a file to upload"));
            }

            // Upload to Supabase Storage
            String fileUrl = storageService.uploadProductImage(file);
            
            return ResponseEntity.ok(fileUrl);
            
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to upload file: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteImage(@RequestParam("url") String imageUrl) {
        try {
            boolean deleted = storageService.deleteFile(imageUrl);
            
            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Image deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("Failed to delete image"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Error deleting image: " + e.getMessage()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkImage(@RequestParam("url") String imageUrl) {
        try {
            boolean exists = storageService.fileExists(imageUrl);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Error checking image: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    // Legacy endpoints for backward compatibility - redirect to Supabase URLs
    // These will no longer serve files but can be used to inform about the migration
    @GetMapping("/profile-photos/{filename}")
    public ResponseEntity<?> getProfilePhoto(@PathVariable String filename) {
        Map<String, String> message = new HashMap<>();
        message.put("message", "Images are now stored in Supabase Storage. Please use the full URL returned from upload endpoint.");
        return ResponseEntity.status(410).body(message); // 410 Gone
    }

    @GetMapping("/products/{filename}")
    public ResponseEntity<?> getProductImage(@PathVariable String filename) {
        Map<String, String> message = new HashMap<>();
        message.put("message", "Images are now stored in Supabase Storage. Please use the full URL returned from upload endpoint.");
        return ResponseEntity.status(410).body(message); // 410 Gone
    }
}
