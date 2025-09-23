package com.example.demo.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_experience")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    @JsonBackReference("employee-experience") // Add this
   
    private Employee employee;
    
    private String company;
    private String role;
    private String department;
    
    @Column(name = "joining_date")
    private LocalDate joiningDate;
    
   
    private String reason;

    @Lob
    private byte[] experienceLetter;
    private String experienceLetterName;

    
    private String pastEmploymentType;
    @Column(name = "last_working_date")
    private String lastWorkingDate; // "Present" or date
    
    private BigDecimal salary;
    private Integer years;
}