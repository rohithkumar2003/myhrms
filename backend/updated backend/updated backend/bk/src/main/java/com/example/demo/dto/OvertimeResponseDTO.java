package com.example.demo.dto;

import java.time.LocalDate;

import com.example.demo.model.Overtime;
import com.example.demo.model.Overtime.OTStatus;
import com.example.demo.model.Overtime.OTType;

public class OvertimeResponseDTO {
    private Long id;
    private String employeeId;
    private String employeeName;
    private LocalDate date;
    private OTType type;
    private OTStatus status;
    private Boolean isUsedAsLeave;
    private Boolean isPaidOut;
    
    // Constructor
    public OvertimeResponseDTO(Overtime overtime) {
        this.id = overtime.getId();
        this.employeeId = overtime.getEmployee().getEmployeeId();
        this.employeeName = overtime.getEmployee().getName();
        this.date = overtime.getDate();
        this.type = overtime.getType();
        this.status = overtime.getStatus();
        this.isUsedAsLeave = overtime.getIsUsedAsLeave();
        this.isPaidOut = overtime.getIsPaidOut();
    }
    
    // Getters and Setters (generate these)
    public Long getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getDate() { return date; }
    public OTType getType() { return type; }
    public OTStatus getStatus() { return status; }
    public Boolean getIsUsedAsLeave() { return isUsedAsLeave; }
    public Boolean getIsPaidOut() { return isPaidOut; }
}