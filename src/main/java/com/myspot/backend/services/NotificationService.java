
package com.myspot.backend.services;

import com.myspot.backend.entities.*;
import com.myspot.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    
    private final PGManagementOwnerRepository pgManagementOwnerRepository;
    private final NotificationRepository notificationRepository;
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllNotifications(Long pgId) {
        log.info("Getting all notifications for PG ID: {}", pgId);
        
        List<Notification> notifications = notificationRepository.findByPgIdOrderByCreatedAtDesc(pgId);
        
        return notifications.stream()
            .map(this::convertNotificationToMap)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUnreadNotifications(Long pgId) {
        log.info("Getting unread notifications for PG ID: {}", pgId);
        
        List<Notification> notifications = notificationRepository.findUnreadByPgId(pgId);
        
        return notifications.stream()
            .map(this::convertNotificationToMap)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long pgId) {
        log.info("Getting unread notification count for PG ID: {}", pgId);
        
        return notificationRepository.countUnreadByPgId(pgId);
    }
    
    public void markAsRead(Long pgId, Long notificationId) {
        log.info("Marking notification {} as read for PG ID: {}", notificationId, pgId);
        
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getPgManagementOwner().getPgId().equals(pgId)) {
            throw new RuntimeException("Notification does not belong to this PG");
        }
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
    
    public void markAllAsRead(Long pgId) {
        log.info("Marking all notifications as read for PG ID: {}", pgId);
        
        List<Notification> unreadNotifications = notificationRepository.findUnreadByPgId(pgId);
        
        LocalDateTime now = LocalDateTime.now();
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });
        
        notificationRepository.saveAll(unreadNotifications);
    }
    
    public void createNotification(Long pgId, String title, String message, 
                                 Notification.NotificationType type, Notification.Priority priority) {
        log.info("Creating notification for PG ID: {}, type: {}", pgId, type);
        
        PGManagementOwner pgManagementOwner = pgManagementOwnerRepository.findById(pgId)
            .orElseThrow(() -> new RuntimeException("PG not found"));
        
        Notification notification = Notification.builder()
            .pgManagementOwner(pgManagementOwner)
            .title(title)
            .message(message)
            .notificationType(type)
            .priority(priority != null ? priority : Notification.Priority.MEDIUM)
            .isRead(false)
            .build();
        
        notificationRepository.save(notification);
    }
    
    private Map<String, Object> convertNotificationToMap(Notification notification) {
        Map<String, Object> notificationMap = new HashMap<>();
        
        notificationMap.put("notificationId", notification.getNotificationId());
        notificationMap.put("title", notification.getTitle());
        notificationMap.put("message", notification.getMessage());
        notificationMap.put("notificationType", notification.getNotificationType().name());
        notificationMap.put("priority", notification.getPriority().name());
        notificationMap.put("isRead", notification.getIsRead());
        notificationMap.put("readAt", notification.getReadAt());
        notificationMap.put("actionUrl", notification.getActionUrl());
        notificationMap.put("relatedEntityId", notification.getRelatedEntityId());
        notificationMap.put("relatedEntityType", notification.getRelatedEntityType());
        notificationMap.put("createdAt", notification.getCreatedAt());
        
        return notificationMap;
    }
}