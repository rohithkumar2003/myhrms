package com.example.demo.dto;

import java.time.LocalDateTime;

public class PunchStatusDTO {
    private boolean punchInEnabled;
    private String currentStatus;
    private LocalDateTime punchInTime;
    private LocalDateTime punchOutTime;
    private String attendanceStatus;
    
    // Constructors
    public PunchStatusDTO() {}
    
    public PunchStatusDTO(boolean punchInEnabled, String currentStatus, LocalDateTime punchInTime, 
                         LocalDateTime punchOutTime, String attendanceStatus) {
        this.punchInEnabled = punchInEnabled;
        this.currentStatus = currentStatus;
        this.punchInTime = punchInTime;
        this.punchOutTime = punchOutTime;
        this.attendanceStatus = attendanceStatus;
    }
    
    // Getters and Setters
    public boolean isPunchInEnabled() { return punchInEnabled; }
    public void setPunchInEnabled(boolean punchInEnabled) { this.punchInEnabled = punchInEnabled; }
    
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    
    public LocalDateTime getPunchInTime() { return punchInTime; }
    public void setPunchInTime(LocalDateTime punchInTime) { this.punchInTime = punchInTime; }
    
    public LocalDateTime getPunchOutTime() { return punchOutTime; }
    public void setPunchOutTime(LocalDateTime punchOutTime) { this.punchOutTime = punchOutTime; }
    
    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
}