package com.example.demo.dto;

import java.time.LocalDateTime;

public class AttendanceResponseDTO {
    private Long id;
    private String employeeId;
    private String employeeName;
    private LocalDateTime punchInTime;
    private LocalDateTime punchOutTime;
    private Double hoursWorked;
    private String status;
    private Double idleTime;
    private Boolean isLateLogin;
    private Boolean isOtDay;
    
    // Constructor
    public AttendanceResponseDTO(Long id, String employeeId, String employeeName, 
                                LocalDateTime punchInTime, LocalDateTime punchOutTime,
                                Double hoursWorked, String status, Double idleTime,
                                Boolean isLateLogin, Boolean isOtDay) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.punchInTime = punchInTime;
        this.punchOutTime = punchOutTime;
        this.hoursWorked = hoursWorked;
        this.status = status;
        this.idleTime = idleTime;
        this.isLateLogin = isLateLogin;
        this.isOtDay = isOtDay;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDateTime getPunchInTime() { return punchInTime; }
    public LocalDateTime getPunchOutTime() { return punchOutTime; }
    public Double getHoursWorked() { return hoursWorked; }
    public String getStatus() { return status; }
    public Double getIdleTime() { return idleTime; }
    public Boolean getIsLateLogin() { return isLateLogin; }
    public Boolean getIsOtDay() { return isOtDay; }
}