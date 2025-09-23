package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String recipientId;
    private String recipientType;
    private String title;
    private String message;
    
    @Column(name = "is_read")
    private boolean read = false;
    
    private String relatedEntityType;
    private Long relatedEntityId;
    
    private String actionUrl;
    
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private NotificationType type;

    public enum NotificationType {
        LEAVE_REQUEST_SUBMITTED,
        LEAVE_REQUEST_APPROVED,
        LEAVE_REQUEST_REJECTED,
        OVERTIME_REQUEST_SUBMITTED,      // Add this
        OVERTIME_REQUEST_APPROVED,       // Add this  
        OVERTIME_REQUEST_REJECTED,       // Add this
        PERMISSION_REQUEST_SUBMITTED,    // Add this
        PERMISSION_REQUEST_APPROVED,     // Add this
        PERMISSION_REQUEST_REJECTED,     // Add this
        ATTENDANCE_ALERT,
        SYSTEM_ALERT
    }

    public Notification() {
        this.createdAt = LocalDateTime.now();
    }

    public Notification(String recipientId, String recipientType, String title, 
                       String message, String relatedEntityType, Long relatedEntityId, 
                       String actionUrl, NotificationType type) {
        this.recipientId = recipientId;
        this.recipientType = recipientType;
        this.title = title;
        this.message = message;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.actionUrl = actionUrl;
        this.type = type;
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public String getRecipientType() { return recipientType; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { 
        this.read = read; 
        if (read) {
            this.readAt = LocalDateTime.now();
        }
    }
    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }
    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
}