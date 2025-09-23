package com.example.demo.dto;

import lombok.Data;

@Data
public class EmployeeRequest {
    private String email;
    private String password;      // ✅ add this field for employee login
    private String role;
    private Boolean enabled;
   
}
