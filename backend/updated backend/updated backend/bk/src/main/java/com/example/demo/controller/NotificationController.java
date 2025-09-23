package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Notification;
import com.example.demo.service.NotificationService;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService service;

    @GetMapping
    public List<Notification> getAllNotifications() {
        // Return all notifications ordered by creation date
        return service.getNotificationsByRecipientType("ALL");
    }

    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications() {
        // Return all unread notifications
        return service.getUnreadNotificationsByRecipientType("ALL");
    }

    @GetMapping("/employee/{employeeId}")
    public List<Notification> getEmployeeNotifications(@PathVariable String employeeId) {
        return service.getNotificationsByRecipient(employeeId);
    }

    @GetMapping("/employee/{employeeId}/unread")
    public List<Notification> getUnreadEmployeeNotifications(@PathVariable String employeeId) {
        return service.getUnreadNotificationsByRecipient(employeeId);
    }

    @GetMapping("/admin")
    public List<Notification> getAdminNotifications() {
        return service.getAdminNotifications();
    }

 // Add this method to your NotificationController
    @GetMapping("/admin/unread-count")
    public Map<String, Long> getUnreadAdminCount() {
        long count = service.getUnreadAdminCount();
        return Map.of("count", count);
    }
    @GetMapping("/admin/unread")
    public List<Notification> getUnreadAdminNotifications() {
        return service.getUnreadAdminNotifications();
    }

    @PostMapping
    public Notification createNotification(@RequestBody NotificationRequest request) {
        return service.createNotification(
            request.getRecipientId(),
            request.getRecipientType(),
            request.getTitle(),
            request.getMessage(),
            request.getRelatedEntityType(),
            request.getRelatedEntityId(),
            request.getActionUrl(),
            request.getType()
        );
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        service.markAsRead(id);
    }

    @PutMapping("/employee/{employeeId}/mark-all-read")
    public void markAllEmployeeAsRead(@PathVariable String employeeId) {
        service.markAllAsRead(employeeId);
    }

    @PutMapping("/admin/mark-all-read")
    public void markAllAdminAsRead() {
        service.markAllAdminAsRead();
    }

    // DTO for notification creation request
    public static class NotificationRequest {
        private String recipientId;
        private String recipientType;
        private String title;
        private String message;
        private String relatedEntityType;
        private Long relatedEntityId;
        private String actionUrl;
        private Notification.NotificationType type;

        // Getters and setters
        public String getRecipientId() { return recipientId; }
        public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
        public String getRecipientType() { return recipientType; }
        public void setRecipientType(String recipientType) { this.recipientType = recipientType; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getRelatedEntityType() { return relatedEntityType; }
        public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }
        public Long getRelatedEntityId() { return relatedEntityId; }
        public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
        public Notification.NotificationType getType() { return type; }
        public void setType(Notification.NotificationType type) { this.type = type; }
    }
}