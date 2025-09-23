package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    public Notification createNotification(String recipientId, String recipientType, String title, 
                                         String message, String relatedEntityType, Long relatedEntityId, 
                                         String actionUrl, Notification.NotificationType type) {
        
        Notification notification = new Notification(
            recipientId, recipientType, title, message, 
            relatedEntityType, relatedEntityId, actionUrl, type
        );
        
        return notificationRepository.save(notification);
    }
    
    // ADD THIS METHOD - Bulk notification creation
    @Transactional
    public void createBulkNotification(List<String> recipientIds, String recipientType, String title, 
                                     String message, String relatedEntityType, Long relatedEntityId, 
                                     String actionUrl, Notification.NotificationType type) {
        
        for (String recipientId : recipientIds) {
            Notification notification = new Notification(
                recipientId, recipientType, title, message, 
                relatedEntityType, relatedEntityId, actionUrl, type
            );
            notificationRepository.save(notification);
        }
    }
    
    public List<Notification> getNotificationsByRecipient(String recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }
    
    public List<Notification> getUnreadNotificationsByRecipient(String recipientId) {
        return notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(recipientId);
    }
    
    public List<Notification> getNotificationsByRecipientType(String recipientType) {
        return notificationRepository.findByRecipientTypeOrderByCreatedAtDesc(recipientType);
    }
    
    public List<Notification> getUnreadNotificationsByRecipientType(String recipientType) {
        return notificationRepository.findByRecipientTypeAndReadFalseOrderByCreatedAtDesc(recipientType);
    }
    
    public List<Notification> getAdminNotifications() {
        return getNotificationsByRecipientType("ADMIN");
    }
    
    public List<Notification> getUnreadAdminNotifications() {
        return getUnreadNotificationsByRecipientType("ADMIN");
    }
    
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
    
    @Transactional
    public void markAllAsRead(String recipientId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(recipientId);
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
    
    @Transactional
    public void markAllAdminAsRead() {
        List<Notification> unreadAdminNotifications = getUnreadAdminNotifications();
        unreadAdminNotifications.forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
    
    public long getUnreadCount(String recipientId) {
        return notificationRepository.countByRecipientIdAndReadFalse(recipientId);
    }
    
    public long getUnreadAdminCount() {
        return notificationRepository.countByRecipientTypeAndReadFalse("ADMIN");
    }
    
    public Notification getNotificationWithAction(Long notificationId) {
        return notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
    }
    
    // Add these methods for backward compatibility with the old controller
    public List<Notification> getAllNotifications() {
        // Return all notifications ordered by creation date
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public List<Notification> getUnreadNotifications() {
        // Return all unread notifications
        return notificationRepository.findByReadFalseOrderByCreatedAtDesc();
    }
    
    public Notification saveNotification(String message) {
        // Simple notification creation for backward compatibility
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setRecipientType("ALL");
        notification.setTitle("System Notification");
        return notificationRepository.save(notification);
    }
    
    public void markAllAsRead() {
        // Mark all notifications as read
        List<Notification> allNotifications = notificationRepository.findAll();
        allNotifications.forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
}