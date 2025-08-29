package com.myspot.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {
    
    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${server.port:9000}")
    private String serverPort;
    
    public String uploadImage(MultipartFile file, String folder) {
        log.info("Uploading image: {} to folder: {}", file.getOriginalFilename(), folder);
        
        try {
            validateImageFile(file);
            
            Path uploadPath = Paths.get(uploadDir, folder);
            Files.createDirectories(uploadPath);
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String fileUrl = String.format("http://localhost:%s/uploads/%s/%s", serverPort, folder, filename);
            log.info("File uploaded successfully: {}", fileUrl);
            
            return fileUrl;
            
        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }
    
    public void deleteFile(String filePath) {
        log.info("Deleting file: {}", filePath);
        
        try {
            if (filePath.startsWith("http://")) {
                String[] parts = filePath.split("/uploads/");
                if (parts.length > 1) {
                    filePath = uploadDir + "/" + parts[1];
                }
            }
            
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            log.info("File deleted successfully: {}", filePath);
            
        } catch (IOException e) {
            log.error("Failed to delete file: {}", e.getMessage(), e);
        }
    }
    
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("File must be an image");
        }
        
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size cannot exceed 10MB");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || !isAllowedImageExtension(filename)) {
            throw new RuntimeException("Only JPEG, PNG, and GIF files are allowed");
        }
    }
    
    private boolean isAllowedImageExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg") || 
               extension.equals("png") || extension.equals("gif");
    }
}