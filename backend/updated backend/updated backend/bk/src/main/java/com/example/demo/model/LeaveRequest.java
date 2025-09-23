package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id") // Make sure this matches your database column name
    private String employeeId;
    
    private String employeeName;
    
    @Column(name = "request_date") // Add this field
    private LocalDate requestDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id", insertable = false, updatable = false)
    private Employee employee;
    
    private LocalDate fromDate;
    private LocalDate toDate;
    
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;
    
    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    private LeaveDayType leaveDayType;
    
    private String halfDaySession;
    private Boolean manualOverride = false;
    private String approvedBy;
    private LocalDateTime actionDate;
    private String remarks;
    private Integer leaveDays;
    
    @OneToMany(mappedBy = "leaveRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LeaveRequestDay> leaveRequestDays;
    
    // Constructors
    public LeaveRequest() { this.requestDate = LocalDate.now();}
    
    public LeaveRequest(String employeeId, String employeeName, LocalDate fromDate, LocalDate toDate, 
                       LeaveType leaveType, LeaveDayType leaveDayType) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.requestDate = LocalDate.now(); 
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.leaveType = leaveType;
        this.leaveDayType = leaveDayType;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public String getEmployeeName() { 
        if (employeeName != null) {
            return employeeName;
        } else if (employee != null) {
            return employee.getName();
        }
        return "Unknown Employee";
    }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    
    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }
    
    public LeaveStatus getStatus() { return status; }
    public void setStatus(LeaveStatus status) { this.status = status; }
    
    public LeaveDayType getLeaveDayType() { return leaveDayType; }
    public void setLeaveDayType(LeaveDayType leaveDayType) { this.leaveDayType = leaveDayType; }
    
    public String getHalfDaySession() { return halfDaySession; }
    public void setHalfDaySession(String halfDaySession) { this.halfDaySession = halfDaySession; }
    
    public Boolean getManualOverride() { return manualOverride; }
    public void setManualOverride(Boolean manualOverride) { this.manualOverride = manualOverride; }
    
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getActionDate() { return actionDate; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }
    
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    
    public Integer getLeaveDays() { return leaveDays; }
    public void setLeaveDays(Integer leaveDays) { this.leaveDays = leaveDays; }
    
    public LocalDate getRequestDate() { return requestDate; } // Add getter
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; } // Add setter

    
    public List<LeaveRequestDay> getLeaveRequestDays() { return leaveRequestDays; }
    public void setLeaveRequestDays(List<LeaveRequestDay> leaveRequestDays) { this.leaveRequestDays = leaveRequestDays; }
    
    // Helper methods
    public boolean isManualOverride() {
        return Boolean.TRUE.equals(manualOverride);
    }
}