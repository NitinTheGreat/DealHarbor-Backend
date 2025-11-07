package com.dealharbor.dealharbor_backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${supabase.storage.project-url}")
    private String projectUrl;

    @Value("${supabase.storage.bucket-name}")
    private String bucketName;

    @Value("${supabase.storage.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.storage.public-url}")
    private String publicUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Upload a file to Supabase Storage using REST API
     * @param file The file to upload
     * @param folder The folder path (e.g., "products", "profile-photos")
     * @return The public URL of the uploaded file
     * @throws IOException If upload fails
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Cannot upload empty file");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Only image files are allowed");
        }

        // Validate file size (max 5MB)
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IOException("File size exceeds maximum limit of 5MB");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;
        String filePath = folder + "/" + uniqueFilename;

        try {
            // Supabase Storage REST API endpoint
            String uploadUrl = projectUrl + "/storage/v1/object/" + bucketName + "/" + filePath;

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            headers.set("apikey", serviceRoleKey);

            // Create request entity with file bytes
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

            // Upload file
            ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                // Return public URL
                return publicUrl + "/" + filePath;
            } else {
                throw new IOException("Failed to upload file. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new IOException("Failed to upload file to Supabase Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file from Supabase Storage
     * @param fileUrl The public URL of the file to delete
     * @return true if deletion was successful
     */
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith(publicUrl)) {
                return false;
            }

            // Extract the file path from the URL
            String filePath = fileUrl.substring(publicUrl.length());
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }

            // Supabase Storage REST API endpoint
            String deleteUrl = projectUrl + "/storage/v1/object/" + bucketName + "/" + filePath;

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            headers.set("apikey", serviceRoleKey);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Delete file
            ResponseEntity<String> response = restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                requestEntity,
                String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Failed to delete file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a file exists in Supabase Storage
     * @param fileUrl The public URL of the file
     * @return true if file exists
     */
    public boolean fileExists(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith(publicUrl)) {
                return false;
            }

            // Try to access the public URL
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                fileUrl,
                HttpMethod.HEAD,
                requestEntity,
                String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Upload product image
     * @param file The image file
     * @return The public URL of the uploaded image
     * @throws IOException If upload fails
     */
    public String uploadProductImage(MultipartFile file) throws IOException {
        return uploadFile(file, "products");
    }

    /**
     * Upload profile photo
     * @param file The image file
     * @return The public URL of the uploaded photo
     * @throws IOException If upload fails
     */
    public String uploadProfilePhoto(MultipartFile file) throws IOException {
        return uploadFile(file, "profile-photos");
    }
}
