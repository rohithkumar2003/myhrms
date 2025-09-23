package com.example.demo.service;

import com.example.demo.model.Employee;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendWelcomeEmail(Employee employee) {
        // Get email from multiple possible sources
        String emailAddress = getEmployeeEmail(employee);
        if (emailAddress == null) return;

        String companyName = "Arah Infotech"; // Replace with dynamic value if needed
        String employeeName = employee.getName() != null ? employee.getName() : "Employee";
        String jobTitle = employee.getJobDetails() != null && employee.getJobDetails().getDesignation() != null
                ? employee.getJobDetails().getDesignation()
                : "Employee";
        String joiningDate = employee.getJobDetails() != null && employee.getJobDetails().getDoj() != null
                ? employee.getJobDetails().getDoj().toString()
                : "N/A";
        String employeeId = employee.getEmployeeId() != null ? employee.getEmployeeId() : "N/A";

        String subject = "Welcome to " + companyName + ", " + employeeName + "! 🎉";

        String text = "Dear " + employeeName + ",\n\n" +
                "We are delighted to welcome you to " + companyName + " as our new " + jobTitle + ". " +
                "We are excited to have you on board and look forward to the skills, ideas, and enthusiasm you will bring to the team.\n\n" +
                "Your official start date is " + joiningDate + ", and your employee ID is " + employeeId + 
                ". Please log in to the HRMS portal to complete your onboarding formalities and access company resources.\n\n" +
                "At " + companyName + ", we believe in teamwork, growth, and innovation. " +
                "We are confident you will have a rewarding journey with us.\n\n" +
                "Once again, welcome to the team — we're glad to have you with us! 🌟\n\n" +
                "Best regards,\n" +
                "HR Team\n" +
                companyName;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailAddress);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    /**
     * Get employee email from multiple possible sources
     */
    private String getEmployeeEmail(Employee emp) {
        // Priority 1: User account email (if User relationship exists)
        if (emp.getUser() != null && emp.getUser().getEmail() != null) {
            return emp.getUser().getEmail();
        }
        
        // Priority 2: Employee's direct email field
        if (emp.getEmail() != null && !emp.getEmail().trim().isEmpty()) {
            return emp.getEmail();
        }
        
        // No email found
        return null;
    }
}