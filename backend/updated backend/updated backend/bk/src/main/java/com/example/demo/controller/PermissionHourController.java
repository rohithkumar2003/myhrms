package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Notification;
import com.example.demo.model.PermissionHours;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.service.NotificationService;
import com.example.demo.service.PermissionHoursService;

@RestController
@RequestMapping("/api/permission-hours")
public class PermissionHourController {

    private final PermissionHoursService service;
    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;

    public PermissionHourController(PermissionHoursService service, 
                                  NotificationService notificationService,
                                  EmployeeRepository employeeRepository) {
        this.service = service;
        this.notificationService = notificationService;
        this.employeeRepository = employeeRepository;
    }

    // Create new Permission Hours request for specific employee with notification
    @PostMapping("/employee/{employeeId}")
    public ResponseEntity<PermissionHours> createRequest(
            @PathVariable String employeeId,
            @RequestBody PermissionHours request) {
        request.setEmployeeId(employeeId);
        PermissionHours savedRequest = service.savePermissionHours(request);
        
        // Get employee name for notification
        String employeeName = employeeRepository.findById(employeeId)
                .map(employee -> employee.getName())
                .orElse("Unknown Employee");
        
        // Send notification to ADMIN
        notificationService.createNotification(
            "ADMIN",
            "ADMIN",
            "New Permission Hours Request Submitted",
            "Employee " + employeeName + " (" + employeeId + 
            ") has requested permission on " + savedRequest.getDate() + 
            " from " + savedRequest.getFromTime() + " to " + savedRequest.getToTime() + 
            ". Reason: " + savedRequest.getReason(),
            "PERMISSION",
            savedRequest.getId(),
            "/api/permission-hours/admin/requests/" + savedRequest.getId(),
            Notification.NotificationType.PERMISSION_REQUEST_SUBMITTED
        );
        
        return ResponseEntity.ok(savedRequest);
    }

    // Get all Permission Hours requests for specific employee
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PermissionHours>> getRequestsByEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(service.getPermissionHoursByEmployee(employeeId));
    }

    // Get requests by employee and status
    @GetMapping("/employee/{employeeId}/status/{status}")
    public ResponseEntity<List<PermissionHours>> getRequestsByEmployeeAndStatus(
            @PathVariable String employeeId,
            @PathVariable String status) {
        try {
            com.example.demo.model.PermissionStatus statusEnum = 
                com.example.demo.model.PermissionStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(service.getPermissionHoursByEmployeeAndStatus(employeeId, statusEnum));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get requests by employee and date
    @GetMapping("/employee/{employeeId}/date/{date}")
    public ResponseEntity<List<PermissionHours>> getRequestsByEmployeeAndDate(
            @PathVariable String employeeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.getPermissionHoursByEmployeeAndDate(employeeId, date));
    }

    // Get requests by employee and date range
    @GetMapping("/employee/{employeeId}/date-range")
    public ResponseEntity<List<PermissionHours>> getRequestsByEmployeeAndDateRange(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(service.getPermissionHoursByEmployeeAndDateRange(employeeId, startDate, endDate));
    }

    // Get all requests (admin only)
    @GetMapping
    public ResponseEntity<List<PermissionHours>> getAllRequests() {
        return ResponseEntity.ok(service.getAllPermissionHours());
    }

    // Get request by ID
    @GetMapping("/{id}")
    public ResponseEntity<PermissionHours> getRequestById(@PathVariable Long id) {
        PermissionHours request = service.getPermissionHoursById(id);
        return request != null ? ResponseEntity.ok(request) : ResponseEntity.notFound().build();
    }

    // Delete request by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        try {
            service.deletePermissionHours(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Update request by Id
    @PutMapping("/{id}")
    public ResponseEntity<PermissionHours> updateRequest(@PathVariable Long id, @RequestBody PermissionHours request) {
        try {
            PermissionHours updatedRequest = service.updatePermissionHours(id, request);
            return updatedRequest != null ? ResponseEntity.ok(updatedRequest) : ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Approve permission request with action details and notification
    @PostMapping("/{id}/approve")
    public ResponseEntity<PermissionHours> approveRequest(
            @PathVariable Long id,
            @RequestParam String actionBy,
            @RequestParam(required = false) String comments) {
                             
        try {
            PermissionHours approvedRequest = service.approvePermissionHours(id, actionBy, comments);
            
            if (approvedRequest != null) {
                // Get employee name for notification
                String employeeName = employeeRepository.findById(approvedRequest.getEmployeeId())
                        .map(employee -> employee.getName())
                        .orElse("Unknown Employee");
                
                // Send notification to EMPLOYEE
                notificationService.createNotification(
                    approvedRequest.getEmployeeId(),
                    "EMPLOYEE",
                    "Permission Request Approved ✓",
                    "Your permission request for " + approvedRequest.getDate() + 
                    " has been APPROVED by " + actionBy,
                    "PERMISSION",
                    approvedRequest.getId(),
                    "/api/permission-hours/employee/requests/" + approvedRequest.getId(),
                    Notification.NotificationType.PERMISSION_REQUEST_APPROVED
                );
            }
            
            return approvedRequest != null ? ResponseEntity.ok(approvedRequest) : ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Reject permission request with action details and notification
    @PostMapping("/{id}/reject")
    public ResponseEntity<PermissionHours> rejectRequest(
            @PathVariable Long id,
            @RequestParam String actionBy,
            @RequestParam(required = false) String comments) {
        
        try {
            PermissionHours rejectedRequest = service.rejectPermissionHours(id, actionBy, comments);
            
            if (rejectedRequest != null) {
                // Get employee name for notification
                String employeeName = employeeRepository.findById(rejectedRequest.getEmployeeId())
                        .map(employee -> employee.getName())
                        .orElse("Unknown Employee");
                
                // Send notification to EMPLOYEE
                notificationService.createNotification(
                    rejectedRequest.getEmployeeId(),
                    "EMPLOYEE",
                    "Permission Request Rejected ✗",
                    "Your permission request for " + rejectedRequest.getDate() + 
                    " has been REJECTED by " + actionBy,
                    "PERMISSION",
                    rejectedRequest.getId(),
                    "/api/permission-hours/employee/requests/" + rejectedRequest.getId(),
                    Notification.NotificationType.PERMISSION_REQUEST_REJECTED
                );
            }
            
            return rejectedRequest != null ? ResponseEntity.ok(rejectedRequest) : ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get pending requests
    @GetMapping("/pending")
    public ResponseEntity<List<PermissionHours>> getPendingRequests() {
        return ResponseEntity.ok(service.getPermissionHoursByStatus(
            com.example.demo.model.PermissionStatus.PENDING));
    }

    // Get requests with actions (approved/rejected)
    @GetMapping("/actions")
    public ResponseEntity<List<PermissionHours>> getRequestsWithActions() {
        return ResponseEntity.ok(service.getRequestsWithActions());
    }

    // Bulk approve/reject with notifications
    @PostMapping("/bulk-action")
    public ResponseEntity<Map<String, Integer>> bulkAction(
            @RequestBody BulkActionRequest bulkRequest) {
        
        int approved = 0;
        int rejected = 0;
        int failed = 0;
        
        for (Long id : bulkRequest.getIds()) {
            try {
                if (bulkRequest.getAction().equalsIgnoreCase("APPROVE")) {
                    PermissionHours approvedRequest = service.approvePermissionHours(id, bulkRequest.getActionBy(), bulkRequest.getComments());
                    if (approvedRequest != null) {
                        // Send notification
                        notificationService.createNotification(
                            approvedRequest.getEmployeeId(),
                            "EMPLOYEE",
                            "Permission Request Approved ✓",
                            "Your permission request has been APPROVED by " + bulkRequest.getActionBy(),
                            "PERMISSION",
                            approvedRequest.getId(),
                            "/api/permission-hours/employee/requests/" + approvedRequest.getId(),
                            Notification.NotificationType.PERMISSION_REQUEST_APPROVED
                        );
                    }
                    approved++;
                } else if (bulkRequest.getAction().equalsIgnoreCase("REJECT")) {
                    PermissionHours rejectedRequest = service.rejectPermissionHours(id, bulkRequest.getActionBy(), bulkRequest.getComments());
                    if (rejectedRequest != null) {
                        // Send notification
                        notificationService.createNotification(
                            rejectedRequest.getEmployeeId(),
                            "EMPLOYEE",
                            "Permission Request Rejected ✗",
                            "Your permission request has been REJECTED by " + bulkRequest.getActionBy(),
                            "PERMISSION",
                            rejectedRequest.getId(),
                            "/api/permission-hours/employee/requests/" + rejectedRequest.getId(),
                            Notification.NotificationType.PERMISSION_REQUEST_REJECTED
                        );
                    }
                    rejected++;
                }
            } catch (Exception e) {
                failed++;
            }
        }
        
        return ResponseEntity.ok(Map.of(
            "approved", approved,
            "rejected", rejected,
            "failed", failed
        ));
    }
    
    // Add redirection endpoints
    @GetMapping("/admin/requests/{id}")
    public ResponseEntity<PermissionHours> getPermissionRequestForAdmin(@PathVariable Long id) {
        PermissionHours request = service.getPermissionHoursById(id);
        return request != null ? ResponseEntity.ok(request) : ResponseEntity.notFound().build();
    }

    @GetMapping("/employee/requests/{id}")
    public ResponseEntity<PermissionHours> getEmployeePermissionRequest(@PathVariable Long id) {
        PermissionHours request = service.getPermissionHoursById(id);
        return request != null ? ResponseEntity.ok(request) : ResponseEntity.notFound().build();
    }
    
    // DTO for bulk action
    public static class BulkActionRequest {
        private List<Long> ids;
        private String action;
        private String actionBy;
        private String comments;
        
        // Getters and setters
        public List<Long> getIds() { return ids; }
        public void setIds(List<Long> ids) { this.ids = ids; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getActionBy() { return actionBy; }
        public void setActionBy(String actionBy) { this.actionBy = actionBy; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }
}