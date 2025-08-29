
package com.myspot.backend.controllers;

import com.myspot.backend.services.NotificationService;
import com.myspot.backend.dto.response.ApiResponse;
import com.myspot.backend.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping
    @Operation(summary = "Get all notifications", description = "Get all notifications for the PG")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllNotifications(
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting all notifications for PG ID: {}", user.getId());
        
        List<Map<String, Object>> notifications = notificationService.getAllNotifications(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved successfully"));
    }
    
    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Get all unread notifications")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting unread notifications for PG ID: {}", user.getId());
        
        List<Map<String, Object>> notifications = notificationService.getUnreadNotifications(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(notifications, "Unread notifications retrieved successfully"));
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Getting unread notification count for PG ID: {}", user.getId());
        
        Long count = notificationService.getUnreadCount(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(count, "Unread count retrieved successfully"));
    }
    
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    public ResponseEntity<ApiResponse<String>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Marking notification {} as read for PG ID: {}", notificationId, user.getId());
        
        notificationService.markAsRead(user.getId(), notificationId);
        
        return ResponseEntity.ok(ApiResponse.success("Read", "Notification marked as read"));
    }
    
    @PutMapping("/mark-all-read")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        log.info("Marking all notifications as read for PG ID: {}", user.getId());
        
        notificationService.markAllAsRead(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success("Read", "All notifications marked as read"));
    }
}
