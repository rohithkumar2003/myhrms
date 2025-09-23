package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "late_login_counter")
public class LateLoginCounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    private Employee employee;
    
    private Integer month;
    private Integer year;
    private Integer lateLoginCount;
    
    // Default constructor
    public LateLoginCounter() {}
    
    // Constructor with Employee, month, and year
    public LateLoginCounter(Employee employee, Integer month, Integer year) {
        this.employee = employee;
        this.month = month;
        this.year = year;
        this.lateLoginCount = 0;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public Integer getLateLoginCount() { return lateLoginCount; }
    public void setLateLoginCount(Integer lateLoginCount) { this.lateLoginCount = lateLoginCount; }
}