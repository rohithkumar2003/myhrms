package com.example.demo.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "overtime")
public class Overtime {
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public OTType getType() {
		return type;
	}

	public void setType(OTType type) {
		this.type = type;
	}

	public OTStatus getStatus() {
		return status;
	}

	public void setStatus(OTStatus status) {
		this.status = status;
	}

	public Boolean getIsUsedAsLeave() {
		return isUsedAsLeave;
	}

	public void setIsUsedAsLeave(Boolean isUsedAsLeave) {
		this.isUsedAsLeave = isUsedAsLeave;
	}

	public Boolean getIsPaidOut() {
		return isPaidOut;
	}

	public void setIsPaidOut(Boolean isPaidOut) {
		this.isPaidOut = isPaidOut;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    @JsonBackReference("employee-overtime") // ← CHANGE TO THIS 
    private Employee employee;
    
    private LocalDate date;
    
    @Enumerated(EnumType.STRING)
    private OTType type; // PENDING_OT, INCENTIVE_OT
    
    @Enumerated(EnumType.STRING) 
    private OTStatus status; // PENDING, APPROVED, REJECTED
    
    private Boolean isUsedAsLeave;
    private Boolean isPaidOut;
    
    public enum OTType {
        PENDING_OT, INCENTIVE_OT
    }
    
    public enum OTStatus {
        PENDING, APPROVED, REJECTED
    }
}