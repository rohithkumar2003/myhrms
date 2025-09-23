package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Notice;
import com.example.demo.model.Notification;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.NoticeRepository;
import com.example.demo.service.NotificationService;

@RestController
@RequestMapping("/api/notices")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class NoticeController {

    @Autowired
    private NoticeRepository noticeRepo;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired // Add this
    private EmployeeRepository employeeRepository;


    @GetMapping
    public List<Notice> getAll() {
        return noticeRepo.findAll();
    }

    // ✅ Single POST method with notification
    @PostMapping
    public ResponseEntity<Notice> createNotice(@RequestBody Notice notice) {
        Notice savedNotice = noticeRepo.save(notice);
        notificationService.saveNotification("New notice posted: " + notice.getTitle());
        
        List<String> allEmployeeIds = employeeRepository.findAllEmployeeIds();
        notificationService.createBulkNotification(
                allEmployeeIds,           // Send to all employees
                "EMPLOYEE",               // Recipient type
                "New Notice Posted", 
                "New notice: " + notice.getTitle() + " - " + (notice.getMessage().length() > 50 ? 
                    notice.getMessage().substring(0, 50) + "..." : notice.getMessage()),
                "NOTICE",                 // relatedEntityType
                savedNotice.getId(),      // relatedEntityId
                "/api/notices/" + savedNotice.getId(), // actionUrl
                Notification.NotificationType.SYSTEM_ALERT
            );
        return new ResponseEntity<>(savedNotice, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notice> update(@PathVariable Long id, @RequestBody Notice n) {
        return noticeRepo.findById(id).map(existing -> {
            existing.setTitle(n.getTitle());
            existing.setMessage(n.getMessage());
            existing.setAuthor(n.getAuthor());
            existing.setDate(n.getDate());
            return ResponseEntity.ok(noticeRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return noticeRepo.findById(id).map(n -> {
            noticeRepo.delete(n);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Notice> getNoticeById(@PathVariable Long id) {
        return noticeRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
