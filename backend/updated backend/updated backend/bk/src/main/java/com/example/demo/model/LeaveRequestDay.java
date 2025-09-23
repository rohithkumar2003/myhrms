package com.example.demo.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "leaveRequestDays")
@Data
public class LeaveRequestDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "leaveRequestId", nullable = false)
    @JsonBackReference("leaveRequest-days")
    private LeaveRequest leaveRequest;
    
    @Column(name = "leaveDate", nullable = false)
    private LocalDate leaveDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "leaveCategory", nullable = false)
    private LeaveCategory leaveCategory = LeaveCategory.UNPAID;
    
    @Column(name = "sandwichFlag")
    private Boolean sandwichFlag = false;
    
    @Column(name = "otCreditUsed")
    private Boolean otCreditUsed = false;
}
