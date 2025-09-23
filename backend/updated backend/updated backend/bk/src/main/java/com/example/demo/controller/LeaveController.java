package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.EmployeeLeaveStatistics;
import com.example.demo.model.LeaveRequest;
import com.example.demo.model.LeaveRequestDay;
import com.example.demo.model.LeaveStatus;
import com.example.demo.model.Notification;
import com.example.demo.service.LeaveService;
import com.example.demo.service.NotificationService;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {
    private final LeaveService leaveService;
    private final NotificationService notificationService;
    
    // Add explicit constructor injection
    public LeaveController(LeaveService leaveService, NotificationService notificationService) {
        this.leaveService = leaveService;
        this.notificationService = notificationService;
    }
    
    @PostMapping
    public ResponseEntity<LeaveRequest> applyForLeave(@RequestBody LeaveRequest leaveRequest) {
        try {
            LeaveRequest savedRequest = leaveService.applyForLeave(leaveRequest);
            
            // Send notification to ADMIN about new leave request
            notificationService.createNotification(
                "ADMIN",                    // recipientId - send to admin role
                "ADMIN",                    // recipientType
                "New Leave Request Submitted", 
                "Employee " + savedRequest.getEmployeeName() + " (" + savedRequest.getEmployeeId() + 
                ") has submitted a leave request from " + savedRequest.getFromDate() + 
                " to " + savedRequest.getToDate() + " for " + savedRequest.getLeaveType() + " leave",
                "LEAVE_REQUEST",            // relatedEntityType
                savedRequest.getId(),       // relatedEntityId
                "/api/leaves/admin/requests/" + savedRequest.getId(), // actionUrl
                Notification.NotificationType.LEAVE_REQUEST_SUBMITTED
            );
            
            return ResponseEntity.ok(savedRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // Create a DTO for status update with remarks
    public static class LeaveStatusUpdate {
        private LeaveStatus status;
        private String approvedBy;
        private String remarks;
        
        // Getters and setters
        public LeaveStatus getStatus() { return status; }
        public void setStatus(LeaveStatus status) { this.status = status; }
        public String getApprovedBy() { return approvedBy; }
        public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }
    
    // New endpoint for status update with remarks (using request body)
    @PutMapping("/{id}/status-with-remarks")
    public ResponseEntity<LeaveRequest> updateLeaveStatusWithRemarks(
            @PathVariable Long id, 
            @RequestBody LeaveStatusUpdate statusUpdate) {
        
        try {
            LeaveRequest updatedRequest = leaveService.updateLeaveStatus(id, statusUpdate.getStatus(), statusUpdate.getApprovedBy());
            
            // Send notification to EMPLOYEE about status change
            String notificationTitle = "";
            String notificationMessage = "";
            Notification.NotificationType notificationType = null;
            
            if (statusUpdate.getStatus() == LeaveStatus.APPROVED) {
                notificationTitle = "Leave Request Approved ✓";
                notificationMessage = "Your leave request from " + updatedRequest.getFromDate() + 
                                    " to " + updatedRequest.getToDate() + " has been APPROVED by " + statusUpdate.getApprovedBy();
                notificationType = Notification.NotificationType.LEAVE_REQUEST_APPROVED;
            } else if (statusUpdate.getStatus() == LeaveStatus.REJECTED) {
                notificationTitle = "Leave Request Rejected ✗";
                notificationMessage = "Your leave request from " + updatedRequest.getFromDate() + 
                                    " to " + updatedRequest.getToDate() + " has been REJECTED by " + statusUpdate.getApprovedBy();
                if (statusUpdate.getRemarks() != null && !statusUpdate.getRemarks().trim().isEmpty()) {
                    notificationMessage += ". Remarks: " + statusUpdate.getRemarks();
                }
                notificationType = Notification.NotificationType.LEAVE_REQUEST_REJECTED;
            }
            
            if (notificationType != null) {
                notificationService.createNotification(
                    updatedRequest.getEmployeeId(), // recipientId
                    "EMPLOYEE",                    // recipientType
                    notificationTitle,
                    notificationMessage,
                    "LEAVE_REQUEST",               // relatedEntityType
                    updatedRequest.getId(),        // relatedEntityId
                    "/api/leaves/employee/requests/" + updatedRequest.getId(), // actionUrl
                    notificationType
                );
            }
            
            return ResponseEntity.ok(updatedRequest);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // FIXED: Keep the original endpoint for backward compatibility - Accept String and convert to enum
 // In your LeaveController, update the updateLeaveStatus method:
    @PutMapping("/{id}/status")
    public ResponseEntity<LeaveRequest> updateLeaveStatus(
            @PathVariable Long id, 
            @RequestParam String status,  
            @RequestParam String approvedBy) {
        
        try {
            // Convert string to LeaveStatus enum with better handling
            LeaveStatus leaveStatus;
            try {
                leaveStatus = LeaveStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Try alternative spellings or case variations
                if ("approved".equalsIgnoreCase(status)) {
                    leaveStatus = LeaveStatus.APPROVED;
                } else if ("rejected".equalsIgnoreCase(status)) {
                    leaveStatus = LeaveStatus.REJECTED;
                } else if ("pending".equalsIgnoreCase(status)) {
                    leaveStatus = LeaveStatus.PENDING;
                } else {
                    return ResponseEntity.badRequest().body(null);
                }
            }
            
            // Debug: Print the converted status
            System.out.println("Converted status: " + leaveStatus);
            
            LeaveRequest updatedRequest = leaveService.updateLeaveStatus(id, leaveStatus, approvedBy);
            
            // Send notification to EMPLOYEE about status change (without remarks)
            String notificationTitle = "";
            String notificationMessage = "";
            Notification.NotificationType notificationType = null;
            
            if (leaveStatus == LeaveStatus.APPROVED) {
                notificationTitle = "Leave Request Approved ✓";
                notificationMessage = "Your leave request from " + updatedRequest.getFromDate() + 
                                    " to " + updatedRequest.getToDate() + " has been APPROVED by " + approvedBy;
                notificationType = Notification.NotificationType.LEAVE_REQUEST_APPROVED;
            } else if (leaveStatus == LeaveStatus.REJECTED) {
                notificationTitle = "Leave Request Rejected ✗";
                notificationMessage = "Your leave request from " + updatedRequest.getFromDate() + 
                                    " to " + updatedRequest.getToDate() + " has been REJECTED by " + approvedBy;
                notificationType = Notification.NotificationType.LEAVE_REQUEST_REJECTED;
            }
            
            if (notificationType != null) {
                notificationService.createNotification(
                    updatedRequest.getEmployeeId(), // recipientId
                    "EMPLOYEE",                    // recipientType
                    notificationTitle,
                    notificationMessage,
                    "LEAVE_REQUEST",               // relatedEntityType
                    updatedRequest.getId(),        // relatedEntityId
                    "/api/leaves/employee/requests/" + updatedRequest.getId(), // actionUrl
                    notificationType
                );
            }
            
            return ResponseEntity.ok(updatedRequest);
        } catch (Exception e) {
            System.out.println("Error in updateLeaveStatus: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
    
 // In LeaveController (constructor already injects LeaveService)

    @GetMapping("/{id}/details")
    public ResponseEntity<List<LeaveRequestDay>> getLeaveDetails(@PathVariable Long id) {
        try {
            List<LeaveRequestDay> days = leaveService.getLeaveRequestDays(id);
            return ResponseEntity.ok(days);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // If you prefer map DTOs instead (string names like "date", "leavecategory"):
    @GetMapping("/{id}/details-map")
    public ResponseEntity<List<Map<String, Object>>> getLeaveDetailsMap(@PathVariable Long id) {
        try {
            List<Map<String,Object>> map = leaveService.getLeaveRequestDaysAsMap(id);
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // In LeaveController.java - Add this method
    @GetMapping("/employee/{employeeId}/sandwich-with-context")
    public ResponseEntity<Map<String, Object>> getSandwichLeavesWithContext(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            Map<String, Object> response = leaveService.getSandwichLeavesWithContext(employeeId, startDate, endDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/employee/{employeeId}/sandwich")
    public ResponseEntity<List<LeaveRequestDay>> getSandwichLeaves(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            List<LeaveRequestDay> sandwichLeaves = leaveService.getSandwichLeaves(employeeId, startDate, endDate);
            return ResponseEntity.ok(sandwichLeaves);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/employee/{employeeId}/stats")
    public ResponseEntity<?> getEmployeeLeaveStats(
            @PathVariable String employeeId,
            @RequestParam int year,
            @RequestParam int month) {

        try {
            Optional<EmployeeLeaveStatistics> stats = leaveService.getEmployeeLeaveStats(employeeId, year, month);

            if (stats.isPresent()) {
                return ResponseEntity.ok(stats.get());
            } else {
                // ✅ Always return JSON instead of 404
                return ResponseEntity.ok(Map.of(
                    "message", "No leave stats available for this employee in the given period"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch leave stats"));
        }
    }

    
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequest>> getEmployeeLeaves(
            @PathVariable String employeeId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        try {
            List<LeaveRequest> leaves = leaveService.getEmployeeLeaves(employeeId, year, month);
            return ResponseEntity.ok(leaves);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // Admin endpoint to get leave request details for notification redirection
    @GetMapping("/admin/requests/{id}")
    public ResponseEntity<LeaveRequest> getLeaveRequestForAdmin(@PathVariable Long id) {
        Optional<LeaveRequest> leaveRequest = leaveService.getLeaveRequestById(id);
        return leaveRequest.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }
    
    // Employee endpoint to get their leave request details for notification redirection
    @GetMapping("/employee/requests/{id}")
    public ResponseEntity<LeaveRequest> getEmployeeLeaveRequest(@PathVariable Long id) {
        Optional<LeaveRequest> leaveRequest = leaveService.getLeaveRequestById(id);
        if (leaveRequest.isPresent()) {
            // Add authorization check here if needed
            return ResponseEntity.ok(leaveRequest.get());
        }
        return ResponseEntity.notFound().build();
    }
}