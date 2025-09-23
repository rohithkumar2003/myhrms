package com.example.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    @JsonIgnore 
    private Employee employee;
    @Column(name = "employee_name")
    private String employeeName;
    public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}
	private LocalDate date;
    private LocalDateTime punchInTime;
    private LocalDateTime punchOutTime;
    private Double hoursWorked;
    private String status; // Absent, Half Day, Present (Late Login), Present (On Time)
    private Double idleTime;
    private Boolean isLateLogin;
    private Boolean isOtDay;
    private Boolean compOffUsed;
    private Boolean manualApproval;
    private String remarks;
    
    // Default constructor
    public Attendance() {}
    
    // Constructor with Employee and Date
    public Attendance(Employee employee, LocalDate date) {
        this.employee = employee;
        this.employeeName = employee.getName();
        this.date = date;
        this.status = "Absent";
        this.idleTime = 9.0;
        this.isLateLogin = false;
        this.isOtDay = false;
        this.compOffUsed = false;
        this.manualApproval = false;
        System.out.println("New Attendance created with status: " + this.status);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalDateTime getPunchInTime() { return punchInTime; }
    public void setPunchInTime(LocalDateTime punchInTime) { this.punchInTime = punchInTime; }
    
    public LocalDateTime getPunchOutTime() { return punchOutTime; }
    public void setPunchOutTime(LocalDateTime punchOutTime) { this.punchOutTime = punchOutTime; }
    
    public Double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Double getIdleTime() { return idleTime; }
    public void setIdleTime(Double idleTime) { this.idleTime = idleTime; }
    
    public Boolean getIsLateLogin() { return isLateLogin; }
    public void setIsLateLogin(Boolean isLateLogin) { this.isLateLogin = isLateLogin; }
    
    public Boolean getIsOtDay() { return isOtDay; }
    public void setIsOtDay(Boolean isOtDay) { this.isOtDay = isOtDay; }
    
    public Boolean getCompOffUsed() { return compOffUsed; }
    public void setCompOffUsed(Boolean compOffUsed) { this.compOffUsed = compOffUsed; }
    
    public Boolean getManualApproval() { return manualApproval; }
    public void setManualApproval(Boolean manualApproval) { this.manualApproval = manualApproval; }
    
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}