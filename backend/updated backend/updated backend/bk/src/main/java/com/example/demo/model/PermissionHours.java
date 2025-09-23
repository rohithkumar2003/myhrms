package com.example.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "permission_hours_requests")
@Data
public class PermissionHours {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String employeeId;
    private LocalDate date;
    private LocalTime fromTime;
    private LocalTime toTime;
    
    @Column(length = 500)
    private String reason;
    
    @Enumerated(EnumType.STRING)
    private PermissionStatus status = PermissionStatus.PENDING;
    
    private String actionBy; // Person who approved/rejected
    private LocalDateTime actionDate; // When action was taken
    private String actionComments; // Comments for approval/rejection
    
    @Column(name = "request_date")
    private LocalDateTime requestDate = LocalDateTime.now();
}

