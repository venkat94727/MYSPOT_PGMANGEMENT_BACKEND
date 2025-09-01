package com.myspot.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class FileUploadExceptionHandler {
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSizeException(
            MaxUploadSizeExceededException exc) {
        log.warn("File upload size exceeded: {}", exc.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "File size exceeds maximum allowed size");
        response.put("message", "Maximum file size is 10MB");
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }
    
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, String>> handleMultipartException(
            MultipartException exc) {
        log.warn("Multipart file upload error: {}", exc.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "File upload failed");
        response.put("message", "Invalid file format or corrupted file");
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleFileUploadRuntimeException(
            RuntimeException exc) {
        if (exc.getMessage().contains("File") || exc.getMessage().contains("upload")) {
            log.warn("File upload runtime error: {}", exc.getMessage());
            
            Map<String, String> response = new HashMap<>();
            response.put("error", "File upload failed");
            response.put("message", exc.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
        throw exc; // Re-throw if not file-related
    }
}
