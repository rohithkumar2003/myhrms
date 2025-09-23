package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Add these missing methods
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);
    
    List<Notification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(String recipientId);
    
    List<Notification> findByRecipientTypeOrderByCreatedAtDesc(String recipientType);
    
    List<Notification> findByRecipientTypeAndReadFalseOrderByCreatedAtDesc(String recipientType);
    
    List<Notification> findByRelatedEntityTypeAndRelatedEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId OR n.recipientType = :recipientType ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdOrType(@Param("recipientId") String recipientId, 
                                              @Param("recipientType") String recipientType);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.read = false")
    long countByRecipientIdAndReadFalse(@Param("recipientId") String recipientId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientType = :recipientType AND n.read = false")
    long countByRecipientTypeAndReadFalse(@Param("recipientType") String recipientType);
    
    // Keep your existing methods
    List<Notification> findByReadFalseOrderByCreatedAtDesc();
    List<Notification> findAllByOrderByCreatedAtDesc();
}