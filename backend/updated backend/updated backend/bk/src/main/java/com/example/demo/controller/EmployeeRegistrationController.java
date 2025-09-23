package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.model.JobDetails;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employee-registration")
@RequiredArgsConstructor
public class EmployeeRegistrationController {

    private final EmployeeService employeeService;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<?> registerEmployee(@RequestBody Employee employee) {
        try {
            // Link nested objects
            if (employee.getPersonalDetails() != null)
                employee.getPersonalDetails().setEmployee(employee);

            if (employee.getBankDetails() != null)
                employee.getBankDetails().setEmployee(employee);

            if (employee.getExperienceDetails() != null)
                employee.getExperienceDetails().forEach(exp -> exp.setEmployee(employee));

            if (employee.getJobDetails() != null) {
                JobDetails job = employee.getJobDetails();
                job.setEmployee(employee);

                // ✅ Ensure deptId is provided
                if (job.getDeptId() == null || job.getDeptId().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "JobDetails deptId cannot be null"));
                }

                // Optional: Ensure DOJ is set
                if (job.getDoj() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "JobDetails date of joining cannot be null"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "JobDetails must be provided"));
            }

            // Default User setup
            if (employee.getUser() != null && employee.getUser().getEmail() != null) {
                User user = employee.getUser();
                if (user.getPassword() == null || user.getPassword().isEmpty())
                    user.setPassword("default123");

                if (user.getRole() == null) user.setRole(UserRole.EMPLOYEE);
                if (user.getEnabled() == null) user.setEnabled(true);

                user.setEmployee(employee); // link user -> employee
            }

            // Save employee
            Employee savedEmployee = employeeService.addEmployee(employee);

            // Send welcome email
            emailService.sendWelcomeEmail(savedEmployee);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to register employee: " + e.getMessage()));
        }
    }
}
