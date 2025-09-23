package com.example.demo.dto;

import com.example.demo.model.Employee;

import lombok.Data;

@Data
public class EmployeeRegistrationRequest {
    private Employee employee;
    private String email;
    private String password;
}