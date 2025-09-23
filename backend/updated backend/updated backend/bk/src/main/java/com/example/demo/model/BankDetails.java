package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_bank_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    @JsonBackReference("employee-bank") 
    private Employee employee;
    
    @Column(name = "account_number")
    private String accountNumber;
    
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "ifsc_code")
    private String ifscCode;
    
    private String branch;
}