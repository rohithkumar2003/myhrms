package com.example.demo.service;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BirthdayEmailService {

    private final EmployeeRepository employeeRepository;
    private final JavaMailSender mailSender;

    public void sendBirthdayEmails() {
        LocalDate today = LocalDate.now();
        int todayMonth = today.getMonthValue();
        int todayDay = today.getDayOfMonth();

        List<Employee> employees = employeeRepository.findAll();

        for (Employee emp : employees) {
            // Check if employee has personal details and date of birth
            if (emp.getPersonalDetails() != null && emp.getPersonalDetails().getDateofBirth() != null) {
                
                LocalDate dob = emp.getPersonalDetails().getDateofBirth();

                // Check if today is the employee's birthday
                if (dob.getMonthValue() == todayMonth && dob.getDayOfMonth() == todayDay) {
                    
                    // Get email address - try multiple sources
                    String emailAddress = getEmployeeEmail(emp);
                    
                    if (emailAddress != null && !emailAddress.trim().isEmpty()) {
                        sendEmail(emailAddress, emp.getName());
                    }
                }
            }
        }
    }

    /**
     * Get employee email from multiple possible sources
     */
    private String getEmployeeEmail(Employee emp) {
        // Priority 1: User account email
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

    private void sendEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);

            // Subject with employee name
            message.setSubject("Happy Birthday, " + name + "! 🎉");

            // Body with formatted content
            String body = "Dear " + name + ",\n\n" +
                    "Wishing you a very Happy Birthday! 🎂✨\n" +
                    "May this special day bring you joy, success, and good health throughout the year. " +
                    "Thank you for being an integral part of our team — we value your contributions and dedication.\n\n" +
                    "Enjoy your day to the fullest! 🎁🎊\n\n" +
                    "Warm regards,\n" +
                    "Arah Infotech";

            message.setText(body);
            mailSender.send(message);
            
            System.out.println("Birthday email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send birthday email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
        }
    }
}