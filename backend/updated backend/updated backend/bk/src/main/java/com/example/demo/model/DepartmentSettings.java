package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "department_settings")
public class DepartmentSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "department_name")
    private String departmentName;
    
    @Column(name = "emp_type")
    private String empType;
    
    @Column(name = "punch_in_start")
    private LocalTime punchInStart;
    
    @Column(name = "punch_out_end")
    private LocalTime punchOutEnd;
    
    @Column(name = "office_start")
    private LocalTime officeStart;
    
    @Column(name = "office_end") 
    private LocalTime officeEnd;
    
    @Column(name = "late_login_threshold")
    private LocalTime lateLoginThreshold;
    
    @Column(name = "half_day_threshold")
    private Double halfDayThreshold;
    
    @Column(name = "full_day_threshold")
    private Double fullDayThreshold;
    
    @Column(name = "morning_half_login")
    private LocalTime morningHalfLogin;
    
    @Column(name = "morning_half_logout")
    private LocalTime morningHalfLogout;
    
    @Column(name = "afternoon_half_login")
    private LocalTime afternoonHalfLogin;
    
    @Column(name = "afternoon_half_logout")
    private LocalTime afternoonHalfLogout;
    
    // Constructors
    public DepartmentSettings() {}
    
    public DepartmentSettings(String departmentName, String empType, LocalTime punchInStart, 
                             LocalTime punchOutEnd, LocalTime officeStart, LocalTime officeEnd, 
                             LocalTime lateLoginThreshold, Double halfDayThreshold, 
                             Double fullDayThreshold, LocalTime morningHalfLogin, 
                             LocalTime morningHalfLogout, LocalTime afternoonHalfLogin, 
                             LocalTime afternoonHalfLogout) {
        this.departmentName = departmentName;
        this.empType = empType;
        this.punchInStart = punchInStart;
        this.punchOutEnd = punchOutEnd;
        this.officeStart = officeStart;
        this.officeEnd = officeEnd;
        this.lateLoginThreshold = lateLoginThreshold;
        this.halfDayThreshold = halfDayThreshold;
        this.fullDayThreshold = fullDayThreshold;
        this.morningHalfLogin = morningHalfLogin;
        this.morningHalfLogout = morningHalfLogout;
        this.afternoonHalfLogin = afternoonHalfLogin;
        this.afternoonHalfLogout = afternoonHalfLogout;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    
    public String getEmpType() { return empType; }
    public void setEmpType(String empType) { this.empType = empType; }
    
    public LocalTime getPunchInStart() { return punchInStart; }
    public void setPunchInStart(LocalTime punchInStart) { this.punchInStart = punchInStart; }
    
    public LocalTime getPunchOutEnd() { return punchOutEnd; }
    public void setPunchOutEnd(LocalTime punchOutEnd) { this.punchOutEnd = punchOutEnd; }
    
    public LocalTime getOfficeStart() { return officeStart; }
    public void setOfficeStart(LocalTime officeStart) { this.officeStart = officeStart; }
    
    public LocalTime getOfficeEnd() { return officeEnd; }
    public void setOfficeEnd(LocalTime officeEnd) { this.officeEnd = officeEnd; }
    
    public LocalTime getLateLoginThreshold() { return lateLoginThreshold; }
    public void setLateLoginThreshold(LocalTime lateLoginThreshold) { this.lateLoginThreshold = lateLoginThreshold; }
    
    public Double getHalfDayThreshold() { return halfDayThreshold; }
    public void setHalfDayThreshold(Double halfDayThreshold) { this.halfDayThreshold = halfDayThreshold; }
    
    public Double getFullDayThreshold() { return fullDayThreshold; }
    public void setFullDayThreshold(Double fullDayThreshold) { this.fullDayThreshold = fullDayThreshold; }
    
    public LocalTime getMorningHalfLogin() { return morningHalfLogin; }
    public void setMorningHalfLogin(LocalTime morningHalfLogin) { this.morningHalfLogin = morningHalfLogin; }
    
    public LocalTime getMorningHalfLogout() { return morningHalfLogout; }
    public void setMorningHalfLogout(LocalTime morningHalfLogout) { this.morningHalfLogout = morningHalfLogout; }
    
    public LocalTime getAfternoonHalfLogin() { return afternoonHalfLogin; }
    public void setAfternoonHalfLogin(LocalTime afternoonHalfLogin) { this.afternoonHalfLogin = afternoonHalfLogin; }
    
    public LocalTime getAfternoonHalfLogout() { return afternoonHalfLogout; }
    public void setAfternoonHalfLogout(LocalTime afternoonHalfLogout) { this.afternoonHalfLogout = afternoonHalfLogout; }
}