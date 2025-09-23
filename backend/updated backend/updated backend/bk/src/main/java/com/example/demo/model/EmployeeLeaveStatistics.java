package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "employeeLeaveStatistics")
@Data
public class EmployeeLeaveStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employeeId", nullable = false)
    private String employeeId;
    
    @Column(name = "year", nullable = false)
    private Integer year;
    
    @Column(name = "month", nullable = false)
    private Integer month;
    
    @Column(name = "totalLeaveRequests")
    private Integer totalLeaveRequests = 0;
    
    @Column(name = "totalLeavesApproved")
    private Double totalLeavesApproved = 0.0;
    
    @Column(name = "fullDayLeavesApproved")
    private Integer fullDayLeavesApproved = 0;
    
    @Column(name = "halfDayLeavesApproved")
    private Integer halfDayLeavesApproved = 0;
    
    @Column(name = "paidLeaveCount")
    private Double paidLeaveCount = 0.0;
    
    @Column(name = "unpaidLeaveCount")
    private Double unpaidLeaveCount = 0.0;
    
    @Column(name = "rejectedLeaveCount")
    private Integer rejectedLeaveCount = 0;
    
    @Column(name = "pendingLeaveCount")
    private Integer pendingLeaveCount = 0;
    
    @Column(name = "sandwichLeaveCount")
    private Integer sandwichLeaveCount = 0;
    
    @Column(name = "manualOverrideCount")
    private Integer manualOverrideCount = 0;
    
    @Column(name = "compOffUsed")
    private Double compOffUsed = 0.0;
    
    @Column(name = "leavesRemaining")
    private Double leavesRemaining = 0.0;
    
    @Column(name = "lastUpdated")
    private LocalDateTime lastUpdated = LocalDateTime.now();
}

