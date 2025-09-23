package com.example.demo.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Attendance;
import com.example.demo.service.AttendanceService;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "http://localhost:5173")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @GetMapping("/punch-status/{employeeId}")
    public ResponseEntity<Map<String, Object>> getPunchStatus(
            @PathVariable String employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Map<String, Object> status;
            if (date != null) {
                status = attendanceService.getPunchStatus(employeeId, date);
            } else {
                status = attendanceService.getPunchStatus(employeeId); // Today
            }
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }    
    @GetMapping("/punch-out-status/{employeeId}")
    public ResponseEntity<Map<String, Object>> getPunchOutStatus(@PathVariable String employeeId) {
        try {
            Map<String, Object> status = attendanceService.getPunchOutStatus(employeeId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/punch-in/{employeeId}")
    public ResponseEntity<?> punchIn(@PathVariable String employeeId) {
        try {
            Attendance attendance = attendanceService.punchIn(employeeId);
            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/punch-out/{employeeId}")
    public ResponseEntity<?> punchOut(@PathVariable String employeeId) {
        try {
            Attendance attendance = attendanceService.punchOut(employeeId);
            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/today/{employeeId}")
    public ResponseEntity<Attendance> getTodayAttendance(@PathVariable String employeeId) {
        try {
            return attendanceService.getTodayAttendance(employeeId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @PostMapping("/test/punch-in/{employeeId}")
    public ResponseEntity<?> testPunchIn(
            @PathVariable String employeeId,
            @RequestParam String punchInTime) {
        try {
            LocalDateTime customTime = LocalDateTime.parse(punchInTime);
            Attendance attendance = attendanceService.manualPunchIn(employeeId, customTime);
            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // In your AttendanceController.java (or wherever you have attendance endpoints)

    @GetMapping("/employee/{employeeId}/hours/summary/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getMonthlyHoursSummary(
            @PathVariable String employeeId,
            @PathVariable int year,
            @PathVariable int month) {
        
        try {
            Map<String, Object> summary = attendanceService.getMonthlyHoursSummary(employeeId, year, month);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Failed to get monthly summary: " + e.getMessage()));
        }
    }

    @GetMapping("/employee/{employeeId}/hours/breakdown/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getMonthlyHoursBreakdown(
            @PathVariable String employeeId,
            @PathVariable int year,
            @PathVariable int month) {
        
        try {
            Map<String, Object> breakdown = attendanceService.getMonthlyHoursBreakdown(employeeId, year, month);
            return ResponseEntity.ok(breakdown);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Failed to get monthly breakdown: " + e.getMessage()));
        }
    }

    @GetMapping("/employee/{employeeId}/hours/summary")
    public ResponseEntity<Map<String, Object>> getMultiMonthHoursSummary(
            @PathVariable String employeeId,
            @RequestParam int startYear,
            @RequestParam int startMonth,
            @RequestParam int endYear,
            @RequestParam int endMonth) {
        
        try {
            Map<String, Object> summary = attendanceService.getMultiMonthHoursSummary(
                employeeId, startYear, startMonth, endYear, endMonth);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Failed to get multi-month summary: " + e.getMessage()));
        }
    }

    @PostMapping("/test/punch-out/{employeeId}")
    public ResponseEntity<?> testPunchOut(
            @PathVariable String employeeId,
            @RequestParam String punchOutTime) {
        try {
            LocalDateTime customTime = LocalDateTime.parse(punchOutTime);
            Attendance attendance = attendanceService.manualPunchOut(employeeId, customTime);
            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/history/{employeeId}")
    public ResponseEntity<List<Attendance>> getAttendanceHistory(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Attendance> history = attendanceService.getAttendanceHistory(employeeId, startDate, endDate);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}