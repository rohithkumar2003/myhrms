package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.OvertimeResponseDTO;
import com.example.demo.model.Employee;
import com.example.demo.model.Notification;
import com.example.demo.model.Overtime;
import com.example.demo.model.Overtime.OTStatus;
import com.example.demo.model.Overtime.OTType;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.OvertimeRepository;
import com.example.demo.service.NotificationService;
import com.example.demo.service.OvertimeService;

@RestController
@RequestMapping("/api/overtime")
@CrossOrigin(origins = "http://localhost:5173")
public class overtimeController {

    @Autowired
    private OvertimeService overtimeService;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private OvertimeRepository overtimeRepository;

    // Updated to return DTO
    @PostMapping("/request")
    public ResponseEntity<OvertimeResponseDTO> requestOvertime(
            @RequestParam String employeeId,
            @RequestParam String date,
            @RequestParam OTType type) {
        
        OvertimeResponseDTO response = overtimeService.requestOvertime(
            employeeId, LocalDate.parse(date), type);
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        String employeeName = employee != null ? employee.getName() : "Unknown Employee";
        
        notificationService.createNotification(
                "ADMIN",
                "ADMIN",
                "New Overtime Request Submitted",
                "Employee " + employeeName + " (" + employeeId + 
                ") has submitted an overtime request for " + response.getDate() + 
                " for " + response.getType() + " type",
                "OVERTIME",
                response.getId(),
                "/api/overtime/admin/requests/" + response.getId(),
                Notification.NotificationType.OVERTIME_REQUEST_SUBMITTED
            );
        return ResponseEntity.ok(response);
    }

    // Updated to return DTO - FIXED method call
    @PostMapping("/approve/{otId}")
    public ResponseEntity<OvertimeResponseDTO> approveOvertime(
            @PathVariable Long otId,
            @RequestParam OTType type) {
        
        OvertimeResponseDTO response = overtimeService.approveOvertime(otId, type);
        
        // FIXED: Proper notification call
        notificationService.createNotification(
                response.getEmployeeId(),
                "EMPLOYEE",
                "Overtime Request Approved ✓",
                "Your overtime request for " + response.getDate() + 
                " has been APPROVED for " + response.getType() + " type",
                "OVERTIME",
                response.getId(),
                "/api/overtime/employee/requests/" + response.getId(),
                Notification.NotificationType.OVERTIME_REQUEST_APPROVED
            );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-ot-update/{employeeId}")
    public ResponseEntity<?> testOtUpdate(@PathVariable String employeeId) {
        try {
            overtimeService.updateOTStatsAfterPunchOut(employeeId, LocalDate.now().minusDays(1), 8.0);
            
            Employee employee = employeeRepository.findById(employeeId).orElseThrow();
            return ResponseEntity.ok(Map.of(
                "otPendingDays", employee.getOtPendingDays(),
                "totalWorkedDays", employee.getTotalWorkedDays(),
                "status", "TEST_COMPLETED"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Updated to return DTO
    @PostMapping("/reject/{otId}")
    public ResponseEntity<OvertimeResponseDTO> rejectOvertime(@PathVariable Long otId) {
        OvertimeResponseDTO response = overtimeService.rejectOvertime(otId);
        
        notificationService.createNotification(
                response.getEmployeeId(),
                "EMPLOYEE",
                "Overtime Request Rejected ✗",
                "Your overtime request for " + response.getDate() + " has been REJECTED",
                "OVERTIME",
                response.getId(),
                "/api/overtime/employee/requests/" + response.getId(),
                Notification.NotificationType.OVERTIME_REQUEST_REJECTED
            );
        
        return ResponseEntity.ok(response);
    }

    // Updated to return DTO
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<OvertimeResponseDTO>> getEmployeeOvertime(@PathVariable String employeeId) {
        List<OvertimeResponseDTO> response = overtimeService.getEmployeeOvertime(employeeId);
        return ResponseEntity.ok(response);
    }

    // Updated to return DTO
    @GetMapping("/pending")
    public ResponseEntity<List<OvertimeResponseDTO>> getPendingOvertime() {
        List<OvertimeResponseDTO> response = overtimeService.getPendingOvertime();
        return ResponseEntity.ok(response);
    }

    // Admin endpoints with DTO responses
    @PostMapping("/admin/allocate")
    public ResponseEntity<?> allocateOvertimeAdmin(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String employeeName,
            @RequestParam String date,
            @RequestParam OTType type,
            @RequestParam(defaultValue = "APPROVED") OTStatus status,
            @RequestParam(required = false) String reason) {
        
        try {
            OvertimeResponseDTO response = overtimeService.allocateOvertimeAdmin(
                employeeId, employeeName, LocalDate.parse(date), type, status, reason);
            
            if (status == OTStatus.APPROVED) {
                notificationService.createNotification(
                    response.getEmployeeId(),
                    "EMPLOYEE",
                    "Overtime Allocated ✓",
                    "Overtime has been allocated to you for " + response.getDate() + 
                    " for " + response.getType() + " type",
                    "OVERTIME",
                    response.getId(),
                    "/api/overtime/employee/requests/" + response.getId(),
                    Notification.NotificationType.OVERTIME_REQUEST_APPROVED
                );
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/bulk-allocate")
    public ResponseEntity<?> bulkAllocateOvertime(
            @RequestParam List<String> employeeIds,
            @RequestParam String date,
            @RequestParam OTType type,
            @RequestParam(defaultValue = "APPROVED") OTStatus status,
            @RequestParam(required = false) String reason) {
        
        try {
            List<OvertimeResponseDTO> response = overtimeService.bulkAllocateOvertime(
                employeeIds, LocalDate.parse(date), type, status, reason);
            
            if (status == OTStatus.APPROVED) {
                for (OvertimeResponseDTO otResponse : response) {
                    if (otResponse != null) {
                        notificationService.createNotification(
                            otResponse.getEmployeeId(),
                            "EMPLOYEE",
                            "Overtime Allocated ✓",
                            "Overtime has been allocated to you for " + otResponse.getDate() + 
                            " for " + otResponse.getType() + " type",
                            "OVERTIME",
                            otResponse.getId(),
                            "/api/overtime/employee/requests/" + otResponse.getId(),
                            Notification.NotificationType.OVERTIME_REQUEST_APPROVED
                        );
                    }
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/allocations")
    public ResponseEntity<List<OvertimeResponseDTO>> getAllocations(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) OTStatus status) {
        
        LocalDate parsedStartDate = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate parsedEndDate = endDate != null ? LocalDate.parse(endDate) : null;
        
        List<OvertimeResponseDTO> response = overtimeService.getAllocations(
            employeeId, parsedStartDate, parsedEndDate, status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/allocations/{allocationId}")
    public ResponseEntity<?> updateAllocation(
            @PathVariable Long allocationId,
            @RequestParam(required = false) OTType type,
            @RequestParam(required = false) OTStatus status,
            @RequestParam(required = false) String reason) {
        
        try {
            OvertimeResponseDTO response = overtimeService.updateAllocation(allocationId, type, status, reason);
            
            if (status != null) {
                String notificationTitle = status == OTStatus.APPROVED ? 
                    "Overtime Updated ✓" : "Overtime Updated";
                String notificationMessage = "Your overtime request has been updated to " + status;
                
                notificationService.createNotification(
                    response.getEmployeeId(),
                    "EMPLOYEE",
                    notificationTitle,
                    notificationMessage,
                    "OVERTIME",
                    response.getId(),
                    "/api/overtime/employee/requests/" + response.getId(),
                    status == OTStatus.APPROVED ? Notification.NotificationType.OVERTIME_REQUEST_APPROVED : 
                                 Notification.NotificationType.SYSTEM_ALERT
                );
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/allocations/{allocationId}")
    public ResponseEntity<?> deleteAllocation(@PathVariable Long allocationId) {
        try {
            Overtime overtime = overtimeRepository.findById(allocationId).orElse(null);
            overtimeService.deleteAllocation(allocationId);
            
            if (overtime != null && overtime.getEmployee() != null) {
                notificationService.createNotification(
                    overtime.getEmployee().getEmployeeId(),
                    "EMPLOYEE",
                    "Overtime Allocation Removed",
                    "Your overtime allocation for " + overtime.getDate() + " has been removed",
                    "OVERTIME",
                    allocationId,
                    "/api/overtime/employee/requests",
                    Notification.NotificationType.SYSTEM_ALERT
                );
            }
            return ResponseEntity.ok(Map.of("message", "OT allocation deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/departments")
    public ResponseEntity<List<String>> getAllDepartments() {
        try {
            List<String> departments = overtimeService.getAllDepartments();
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @GetMapping("/admin/employees")
    public ResponseEntity<List<Employee>> getEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {
        
        try {
            List<Employee> employees;
            
            if (department != null && search != null) {
                employees = overtimeService.searchEmployeesByDepartment(search, department);
            } else if (department != null) {
                employees = overtimeService.getEmployeesByDepartment(department);
            } else if (search != null) {
                employees = employeeRepository.searchEmployees(search);
            } else {
                employees = employeeRepository.findAll();
            }
            
            if (activeOnly) {
                employees = employees.stream()
                        .filter(emp -> emp.getIsActive() != null && emp.getIsActive())
                        .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @PostMapping("/admin/allocate-department")
    public ResponseEntity<?> allocateOvertimeToDepartment(
            @RequestParam String department,
            @RequestParam String date,
            @RequestParam OTType type,
            @RequestParam(defaultValue = "APPROVED") OTStatus status,
            @RequestParam(required = false) String reason) {
        
        try {
            List<OvertimeResponseDTO> response = overtimeService.allocateOvertimeToDepartment(
                department, LocalDate.parse(date), type, status, reason);
            if (status == OTStatus.APPROVED) {
                for (OvertimeResponseDTO otResponse : response) {
                    if (otResponse != null) {
                        notificationService.createNotification(
                            otResponse.getEmployeeId(),
                            "EMPLOYEE",
                            "Overtime Allocated ✓",
                            "Overtime has been allocated to you for " + otResponse.getDate() + 
                            " for " + otResponse.getType() + " type",
                            "OVERTIME",
                            otResponse.getId(),
                            "/api/overtime/employee/requests/" + otResponse.getId(),
                            Notification.NotificationType.OVERTIME_REQUEST_APPROVED
                        );
                    }
                }
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Add redirection endpoints - FIXED
    @GetMapping("/admin/requests/{id}")
    public ResponseEntity<OvertimeResponseDTO> getOvertimeRequestForAdmin(@PathVariable Long id) {
        try {
            Overtime overtime = overtimeRepository.findById(id).orElse(null);
            if (overtime != null) {
                // Use the constructor directly
                OvertimeResponseDTO response = new OvertimeResponseDTO(overtime);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/employee/requests/{id}")
    public ResponseEntity<OvertimeResponseDTO> getEmployeeOvertimeRequest(@PathVariable Long id) {
        try {
            Overtime overtime = overtimeRepository.findById(id).orElse(null);
            if (overtime != null) {
                // Use the constructor directly
                OvertimeResponseDTO response = new OvertimeResponseDTO(overtime);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}