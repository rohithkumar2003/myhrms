package com.example.demo.service;

import com.example.demo.model.PermissionHours;
import com.example.demo.model.PermissionStatus;
import com.example.demo.model.Attendance;
import com.example.demo.model.DepartmentSettings;
import com.example.demo.model.Employee;
import com.example.demo.repository.PermissionHourRepository;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.DepartmentSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class PermissionHoursService {
    
    private final PermissionHourRepository repository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentSettingsRepository departmentSettingsRepository;

    public PermissionHoursService(PermissionHourRepository repository,
                                 AttendanceRepository attendanceRepository,
                                 EmployeeRepository employeeRepository,
                                 DepartmentSettingsRepository departmentSettingsRepository) {
        this.repository = repository;
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.departmentSettingsRepository = departmentSettingsRepository;
    }

    // ✅ Save a new Permission Hours request
    public PermissionHours savePermissionHours(PermissionHours request) {
        request.setRequestDate(LocalDateTime.now());
        request.setStatus(PermissionStatus.PENDING);
        return repository.save(request);
    }

    // ✅ Get all Permission Hours requests
    public List<PermissionHours> getAllPermissionHours() {
        return repository.findAll();
    }

    // ✅ Get a single Permission Hours request by ID
    public PermissionHours getPermissionHoursById(Long id) {
        return repository.findById(id).orElse(null);
    }

    // ✅ Update Permission Hours request
    public PermissionHours updatePermissionHours(Long id, PermissionHours updatedRequest) {
        Optional<PermissionHours> existingRequestOptional = repository.findById(id);

        if (existingRequestOptional.isPresent()) {
            PermissionHours existingRequest = existingRequestOptional.get();
            if (existingRequest.getStatus() == PermissionStatus.PENDING) {
                existingRequest.setDate(updatedRequest.getDate());
                existingRequest.setFromTime(updatedRequest.getFromTime());
                existingRequest.setToTime(updatedRequest.getToTime());
                existingRequest.setReason(updatedRequest.getReason());
                return repository.save(existingRequest);
            } else {
                throw new IllegalStateException("Cannot update request that is already " + existingRequest.getStatus());
            }
        }
        return null;
    }

    // ✅ Delete a Permission Hours request by ID
    public void deletePermissionHours(Long id) {
        Optional<PermissionHours> request = repository.findById(id);
        if (request.isPresent() && request.get().getStatus() == PermissionStatus.PENDING) {
            repository.deleteById(id);
        } else {
            throw new IllegalStateException("Cannot delete request that is not PENDING");
        }
    }

    // ✅ Approve permission hours request with action details
    @Transactional
    public PermissionHours approvePermissionHours(Long id, String approvedBy, String comments) {
        Optional<PermissionHours> requestOptional = repository.findById(id);
        
        if (requestOptional.isPresent()) {
            PermissionHours request = requestOptional.get();
            
            if (request.getStatus() != PermissionStatus.PENDING) {
                throw new IllegalStateException("Cannot approve request that is not PENDING");
            }
            
            request.setStatus(PermissionStatus.APPROVED);
            request.setActionBy(approvedBy);
            request.setActionDate(LocalDateTime.now());
            request.setActionComments(comments);
            
            // Update attendance record with adjusted punch times
            adjustAttendanceForPermissionHours(request);
            
            return repository.save(request);
        }
        return null;
    }

    // ✅ Reject permission hours request with action details
    @Transactional
    public PermissionHours rejectPermissionHours(Long id, String rejectedBy, String comments) {
        Optional<PermissionHours> requestOptional = repository.findById(id);
        
        if (requestOptional.isPresent()) {
            PermissionHours request = requestOptional.get();
            
            if (request.getStatus() != PermissionStatus.PENDING) {
                throw new IllegalStateException("Cannot reject request that is not PENDING");
            }
            
            request.setStatus(PermissionStatus.REJECTED);
            request.setActionBy(rejectedBy);
            request.setActionDate(LocalDateTime.now());
            request.setActionComments(comments);
            
            return repository.save(request);
        }
        return null;
    }

    // ✅ Get requests by employee
    public List<PermissionHours> getPermissionHoursByEmployee(String employeeId) {
        return repository.findByEmployeeId(employeeId);
    }

    // ✅ Get requests by status
    public List<PermissionHours> getPermissionHoursByStatus(PermissionStatus status) {
        return repository.findByStatus(status);
    }

    // ✅ Get requests by date
    public List<PermissionHours> getPermissionHoursByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    // ✅ Get requests by employee and status
    public List<PermissionHours> getPermissionHoursByEmployeeAndStatus(String employeeId, PermissionStatus status) {
        return repository.findByEmployeeIdAndStatus(employeeId, status);
    }

    // ✅ Get requests by employee and date
    public List<PermissionHours> getPermissionHoursByEmployeeAndDate(String employeeId, LocalDate date) {
        return repository.findByEmployeeIdAndDate(employeeId, date);
    }

    // ✅ Get requests by employee and date range
    public List<PermissionHours> getPermissionHoursByEmployeeAndDateRange(String employeeId, LocalDate startDate, LocalDate endDate) {
        return repository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
    }

    // ✅ Get requests with action details
    public List<PermissionHours> getRequestsWithActions() {
        return repository.findByStatusIn(List.of(PermissionStatus.APPROVED, PermissionStatus.REJECTED));
    }

    // ... [Keep all the private helper methods from your previous implementation] ...
    // adjustAttendanceForPermissionHours, resetAttendanceStatusFields, recalculateFullAttendanceStatus,
    // getDepartmentSettingsDirectly, getDefaultDepartmentSettings, calculateAttendanceStatusManually,
    // recalculateLateLoginStatus, forceRecalculateAttendanceStatus

    // Adjust attendance based on approved permission hours
    private void adjustAttendanceForPermissionHours(PermissionHours permissionHours) {
        String employeeId = permissionHours.getEmployeeId();
        LocalDate date = permissionHours.getDate();
        LocalTime fromTime = permissionHours.getFromTime();
        LocalTime toTime = permissionHours.getToTime();
        
        try {
            // Get the existing attendance record
            Optional<Attendance> attendanceOpt = attendanceRepository.findByEmployeeEmployeeIdAndDate(employeeId, date);
            
            if (attendanceOpt.isPresent()) {
                Attendance attendance = attendanceOpt.get();
                
                // Store original times for comparison
                LocalDateTime originalPunchIn = attendance.getPunchInTime();
                LocalDateTime originalPunchOut = attendance.getPunchOutTime();
                
                boolean needsRecalculation = false;
                
                // Adjust punch in time if permission fromTime is earlier
                if (originalPunchIn != null && fromTime.isBefore(originalPunchIn.toLocalTime())) {
                    LocalDateTime newPunchInTime = LocalDateTime.of(date, fromTime);
                    attendance.setPunchInTime(newPunchInTime);
                    needsRecalculation = true;
                    System.out.println("DEBUG: Adjusted punch-in from " + originalPunchIn + " to " + newPunchInTime);
                }
                
                // Adjust punch out time if permission toTime is later
                if (originalPunchOut != null && toTime.isAfter(originalPunchOut.toLocalTime())) {
                    LocalDateTime newPunchOutTime = LocalDateTime.of(date, toTime);
                    attendance.setPunchOutTime(newPunchOutTime);
                    needsRecalculation = true;
                    System.out.println("DEBUG: Adjusted punch-out from " + originalPunchOut + " to " + newPunchOutTime);
                }
                
                // If adjustments were made, recalculate all attendance fields
                if (needsRecalculation) {
                    // Recalculate hours worked
                    Duration duration = Duration.between(attendance.getPunchInTime(), attendance.getPunchOutTime());
                    double hoursWorked = duration.toMinutes() / 60.0;
                    attendance.setHoursWorked(hoursWorked);
                    
                    // CRITICAL: Reset ALL status fields before complete recalculation
                    resetAttendanceStatusFields(attendance);
                    
                    // Recalculate ALL attendance status from scratch
                    recalculateFullAttendanceStatus(attendance);
                    
                    attendanceRepository.save(attendance);
                    
                    System.out.println("DEBUG: Attendance fully recalculated after permission approval");
                    System.out.println("DEBUG: Final status: " + attendance.getStatus());
                    System.out.println("DEBUG: Final hours: " + attendance.getHoursWorked());
                }
            } else {
                // Create new attendance record if none exists
                LocalDateTime punchInTime = LocalDateTime.of(date, fromTime);
                LocalDateTime punchOutTime = LocalDateTime.of(date, toTime);
                
                // Get employee
                Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));
                
                // Create attendance using your existing constructor
                Attendance attendance = new Attendance(employee, date);
                attendance.setPunchInTime(punchInTime);
                attendance.setPunchOutTime(punchOutTime);
                
                // Calculate hours worked
                Duration duration = Duration.between(punchInTime, punchOutTime);
                double hoursWorked = duration.toMinutes() / 60.0;
                attendance.setHoursWorked(hoursWorked);
                
                // Calculate full status including late login
                recalculateFullAttendanceStatus(attendance);
                
                attendanceRepository.save(attendance);
                
                System.out.println("DEBUG: New attendance created with permission hours");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to adjust attendance for permission hours: " + e.getMessage(), e);
        }
    }

    // Reset all calculated status fields to ensure fresh calculation
    private void resetAttendanceStatusFields(Attendance attendance) {
        attendance.setStatus(null);
        attendance.setIsLateLogin(false);
        attendance.setIdleTime(0.0);
        attendance.setRemarks("Updated from permission hours approval");
        System.out.println("DEBUG: Reset all status fields for fresh calculation");
    }

    // New method to fully recalculate attendance status from scratch
    private void recalculateFullAttendanceStatus(Attendance attendance) {
        System.out.println("=== STARTING COMPLETE STATUS RECALCULATION ===");
        System.out.println("Input - PunchIn: " + attendance.getPunchInTime() + 
                          ", PunchOut: " + attendance.getPunchOutTime() +
                          ", Hours: " + attendance.getHoursWorked());
        
        // Get department settings directly
        DepartmentSettings settings = getDepartmentSettingsDirectly(attendance.getEmployee().getEmployeeId());
        
        // Manual status calculation
        calculateAttendanceStatusManually(attendance, settings);
        
        // Then specifically recalculate late login status
        recalculateLateLoginStatus(attendance, settings);
        
        System.out.println("=== RECALCULATION COMPLETE ===");
        System.out.println("Final Status: " + attendance.getStatus());
        System.out.println("Final Hours: " + attendance.getHoursWorked());
        System.out.println("Is Late Login: " + attendance.getIsLateLogin());
        System.out.println("Idle Time: " + attendance.getIdleTime());
    }

    // Get department settings directly from repository
 // In PermissionHoursService.java - replace the getDepartmentSettingsDirectly method:

    private DepartmentSettings getDepartmentSettingsDirectly(String employeeId) {
        try {
            Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));
            
            if (employee.getJobDetails() != null && employee.getJobDetails().getDeptId() != null) {
                String departmentId = employee.getJobDetails().getDeptId();
                
                // Get all settings and filter manually
                List<DepartmentSettings> allSettings = departmentSettingsRepository.findAll();
                
                return allSettings.stream()
                    .filter(setting -> {
                        // Try different possible field names for department ID
                        String settingDeptId = null;
                        
                        // Option 1: getDepartmentId() method
                        try {
                            java.lang.reflect.Method method = setting.getClass().getMethod("getDepartmentId");
                            settingDeptId = (String) method.invoke(setting);
                        } catch (Exception e) {
                            // Method doesn't exist, try other options
                        }
                        
                        // Option 2: getId() method
                        if (settingDeptId == null) {
                            try {
                                java.lang.reflect.Method method = setting.getClass().getMethod("getId");
                                Object result = method.invoke(setting);
                                if (result != null) {
                                    settingDeptId = result.toString();
                                }
                            } catch (Exception e) {
                                // Method doesn't exist
                            }
                        }
                        
                        // Option 3: Check if there's a deptId field directly
                        if (settingDeptId == null) {
                            try {
                                java.lang.reflect.Field field = setting.getClass().getDeclaredField("deptId");
                                field.setAccessible(true);
                                settingDeptId = (String) field.get(setting);
                            } catch (Exception e) {
                                // Field doesn't exist
                            }
                        }
                        
                        return settingDeptId != null && settingDeptId.equals(departmentId);
                    })
                    .findFirst()
                    .orElse(getDefaultDepartmentSettings());
            }
        } catch (Exception e) {
            System.out.println("Error getting department settings: " + e.getMessage());
        }
        return getDefaultDepartmentSettings();
    }

    // Default department settings fallback
    private DepartmentSettings getDefaultDepartmentSettings() {
        DepartmentSettings defaultSettings = new DepartmentSettings();
        defaultSettings.setHalfDayThreshold(4.0);
        defaultSettings.setFullDayThreshold(8.0);
        defaultSettings.setLateLoginThreshold(LocalTime.of(9, 30));
        return defaultSettings;
    }

    // Manual attendance status calculation
    private void calculateAttendanceStatusManually(Attendance attendance, DepartmentSettings settings) {
        double hoursWorked = attendance.getHoursWorked();
        
        if (hoursWorked < settings.getHalfDayThreshold()) {
            attendance.setStatus("Absent");
            attendance.setIdleTime(settings.getFullDayThreshold());
        } else if (hoursWorked >= settings.getHalfDayThreshold() && hoursWorked < settings.getFullDayThreshold()) {
            attendance.setStatus("Half Day");
            attendance.setIdleTime(settings.getFullDayThreshold() - hoursWorked);
        } else {
            attendance.setStatus("Present (On Time)");
            attendance.setIdleTime(Math.max(0, settings.getFullDayThreshold() - hoursWorked));
        }
    }

    // Method to specifically recalculate late login status
    private void recalculateLateLoginStatus(Attendance attendance, DepartmentSettings settings) {
        if (attendance.getPunchInTime() == null) {
            attendance.setIsLateLogin(false);
            return;
        }
        
        LocalTime punchInTime = attendance.getPunchInTime().toLocalTime();
        boolean isLateLogin = punchInTime.isAfter(settings.getLateLoginThreshold());
        attendance.setIsLateLogin(isLateLogin);
        
        System.out.println("DEBUG: Late login calculation - " + 
                          "PunchIn: " + punchInTime + 
                          ", Threshold: " + settings.getLateLoginThreshold() +
                          ", IsLate: " + isLateLogin);
    }

    // Force recalculation method to bypass any bugs
    private void forceRecalculateAttendanceStatus(Attendance attendance) {
        try {
            // Get fresh department settings
            DepartmentSettings settings = getDepartmentSettingsDirectly(attendance.getEmployee().getEmployeeId());
            
            double hoursWorked = attendance.getHoursWorked();
            System.out.println("FORCE RECALCULATION - Hours: " + hoursWorked + 
                              ", HalfDayThresh: " + settings.getHalfDayThreshold() +
                              ", FullDayThresh: " + settings.getFullDayThreshold());
            
            // Manual status calculation
            calculateAttendanceStatusManually(attendance, settings);
            
            // Recalculate late login
            if (attendance.getPunchInTime() != null) {
                LocalTime punchInTime = attendance.getPunchInTime().toLocalTime();
                boolean isLateLogin = punchInTime.isAfter(settings.getLateLoginThreshold());
                attendance.setIsLateLogin(isLateLogin);
            }
            
            System.out.println("FORCE RECALCULATION RESULT: " + attendance.getStatus());
            
        } catch (Exception e) {
            System.out.println("Error in force recalculation: " + e.getMessage());
            // Fallback to manual calculation with default settings
            calculateAttendanceStatusManually(attendance, getDefaultDepartmentSettings());
        }
    }
}