package com.myspot.backend.repository;

import com.myspot.backend.entities.Notification;
import com.myspot.backend.entities.PGManagementOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByPgManagementOwner(PGManagementOwner pgManagementOwner);
    List<Notification> findByPgManagementOwner_PgId(Long pgId);
    List<Notification> findByPgManagementOwnerOrderByCreatedAtDesc(PGManagementOwner pgManagementOwner);
    
    @Query("SELECT n FROM Notification n WHERE n.pgManagementOwner.pgId = :pgId ORDER BY n.createdAt DESC")
    List<Notification> findByPgIdOrderByCreatedAtDesc(@Param("pgId") Long pgId);
    
    @Query("SELECT n FROM Notification n WHERE n.pgManagementOwner.pgId = :pgId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByPgId(@Param("pgId") Long pgId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.pgManagementOwner.pgId = :pgId AND n.isRead = false")
    Long countUnreadByPgId(@Param("pgId") Long pgId);
    
    List<Notification> findByPgManagementOwnerAndNotificationType(PGManagementOwner pgManagementOwner, Notification.NotificationType type);
}
