package com.example.demo.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.EmployeeDTO;
import com.example.demo.model.BankDetails;
import com.example.demo.model.Employee;
import com.example.demo.model.Experience;
import com.example.demo.model.JobDetails;
import com.example.demo.model.PersonalDetails;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.ExperienceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.NotificationService;
import com.example.demo.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final ExperienceRepository experienceRepository;
   private final PasswordEncoder passwordEncoder; 
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // ------------------- Add Employee -------------------
    @PostMapping
    public ResponseEntity<?> addEmployee(@RequestBody Employee employee) {
        try {
            // ------------------- Link nested objects -------------------
            if (employee.getPersonalDetails() != null)
                employee.getPersonalDetails().setEmployee(employee);

            if (employee.getBankDetails() != null)
                employee.getBankDetails().setEmployee(employee);

            if (employee.getExperienceDetails() != null)
                employee.getExperienceDetails().forEach(exp -> exp.setEmployee(employee));

            if (employee.getJobDetails() != null) {
                JobDetails job = employee.getJobDetails();
                job.setEmployee(employee);

                if (job.getDeptId() == null || job.getDeptId().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "JobDetails deptId cannot be null or empty"));
                }

                if (job.getDoj() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "JobDetails date of joining cannot be null"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "JobDetails must be provided"));
            }

            // ------------------- Default User setup -------------------
            if (employee.getUser() != null && employee.getUser().getEmail() != null) {
                String email = employee.getUser().getEmail();

                // ✅ Duplicate email check
                if (userRepository.existsByEmail(email)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", "Email already exists"));
                }

                User user = employee.getUser();

                if (user.getPassword() == null || user.getPassword().isEmpty())
                    user.setPassword("default123");

                if (user.getRole() == null) user.setRole(UserRole.EMPLOYEE);
                if (user.getEnabled() == null) user.setEnabled(true);

                user.setEmployee(employee);
            }

            // ------------------- Save Employee -------------------
            Employee savedEmployee = employeeService.addEmployee(employee);

            // ------------------- Send Welcome Email -------------------
            emailService.sendWelcomeEmail(savedEmployee);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create employee: " + e.getMessage()));
        }
    }

    // ------------------- Simple Employee Add via DTO -------------------
    @PostMapping("/simple")
    public ResponseEntity<?> addEmployeeSimple(@RequestBody Map<String, String> request) {
        try {
            Employee employee = new Employee();

            User user = new User();
            user.setEmail(request.get("email"));
            user.setPassword(request.getOrDefault("password", "default123"));
            user.setRole(UserRole.EMPLOYEE);
            user.setEnabled(true);

            employee.setUser(user);

            Employee savedEmployee = employeeService.addEmployee(employee);

            // Send welcome email
            emailService.sendWelcomeEmail(savedEmployee);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create employee: " + e.getMessage()));
        }
    }

    // ------------------- Employee Login -------------------
    @PostMapping("/login")
    public ResponseEntity<?> loginEmployee(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        Optional<Employee> employeeOpt = employeeRepository.findByUserEmail(email);

        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            if (employee.getUser() != null && passwordEncoder.matches(password, employee.getUser().getPassword())) {
                
                // ✅ Load UserDetails for token generation
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                String token = jwtUtil.generateToken(userDetails);

                return ResponseEntity.ok(Map.of(
                        "message", "Login successful",
                        "token", token,
                        "employeeId", employee.getEmployeeId(),
                        "email", employee.getUser().getEmail(),
                        "role", employee.getUser().getRole().name()
                ));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid email or password"));
    }
  //---------------------Employee details only to that employee
    
    @GetMapping("/me")
    public ResponseEntity<Employee> getCurrentEmployee(@RequestParam String employeeId) {
        
    	Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/me/bank-details")
    public ResponseEntity<List<BankDetails>> getMyBankDetails(@RequestParam String employeeId) {
        
    	Employee employee = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(employeeService.getAllBankDetails(employeeId));
    }

    @PostMapping("/me/bank-details")
    public ResponseEntity<BankDetails> createMyBankDetails(
    		 @RequestParam String employeeId,
            @RequestBody BankDetails bankDetails) {
       
    	Employee employee = employeeService.getEmployeeById(employeeId);
        bankDetails.setEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.saveBankDetails(bankDetails));
    }

 // ------------------- Employee Self-Service Endpoints -------------------

 // Job Details
 @GetMapping("/me/job-details")
 public ResponseEntity<JobDetails> getMyJobDetails(@RequestParam String employeeId) {
     return ResponseEntity.ok(employeeService.getJobDetails(employeeId));
 }

 @PostMapping("/me/job-details")
 public ResponseEntity<JobDetails> createMyJobDetails(
         @RequestParam String employeeId,
         @RequestBody JobDetails jobDetails) {
     Employee employee = employeeService.getEmployeeById(employeeId);
     jobDetails.setEmployee(employee);
     return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.saveJobDetails(jobDetails));
 }

 @PutMapping("/me/job-details")
 public ResponseEntity<JobDetails> updateMyJobDetails(
         @RequestParam String employeeId,
         @RequestBody JobDetails jobDetails) {
     JobDetails existing = employeeService.getJobDetails(employeeId);
     existing.setDeptId(jobDetails.getDeptId());
     existing.setDepartment(jobDetails.getDepartment());
     existing.setDesignation(jobDetails.getDesignation());
     existing.setDoj(jobDetails.getDoj());
     return ResponseEntity.ok(employeeService.saveJobDetails(existing));
 }

 @DeleteMapping("/me/job-details")
 public ResponseEntity<Void> deleteMyJobDetails(@RequestParam String employeeId) {
     employeeService.deleteJobDetails(employeeId);
     return ResponseEntity.noContent().build();
 }

 // Personal Details
 @GetMapping("/me/personal-details")
 public ResponseEntity<PersonalDetails> getMyPersonalDetails(@RequestParam String employeeId) {
     return ResponseEntity.ok(employeeService.getPersonalDetails(employeeId));
 }

 @PutMapping("/me/personal-details")
 public ResponseEntity<PersonalDetails> updateMyPersonalDetails(
         @RequestParam String employeeId,
         @RequestBody PersonalDetails personalDetails) {
     PersonalDetails existing = employeeService.getPersonalDetails(employeeId);
     existing.setDateofBirth(personalDetails.getDateofBirth());
     existing.setGender(personalDetails.getGender());
     existing.setFatherName(personalDetails.getFatherName());
     existing.setPanNumber(personalDetails.getPanNumber());
     existing.setAadhaarNumber(personalDetails.getAadhaarNumber());
     existing.setMaritalStatus(personalDetails.getMaritalStatus());
     existing.setNationality(personalDetails.getNationality());
     return ResponseEntity.ok(employeeService.savePersonalDetails(existing));
 }

 @DeleteMapping("/me/personal-details")
 public ResponseEntity<Void> deleteMyPersonalDetails(@RequestParam String employeeId) {
     employeeService.deletePersonalDetails(employeeId);
     return ResponseEntity.noContent().build();
 }

 // Experience
 @GetMapping("/me/experience")
 public ResponseEntity<List<Experience>> getMyExperience(@RequestParam String employeeId) {
     return ResponseEntity.ok(employeeService.getExperienceDetails(employeeId));
 }

 @PostMapping("/me/experience")
 public ResponseEntity<Experience> createMyExperience(
         @RequestParam String employeeId,
         @RequestBody Experience experience) {
     Employee employee = employeeService.getEmployeeById(employeeId);
     experience.setEmployee(employee);
     return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.saveExperience(experience));
 }

 @DeleteMapping("/me/experience")
 public ResponseEntity<Void> deleteAllMyExperience(@RequestParam String employeeId) {
     employeeService.deleteAllExperience(employeeId);
     return ResponseEntity.noContent().build();
 }

 @DeleteMapping("/me/experience/{experienceId}")
 public ResponseEntity<Void> deleteMyExperienceById(@PathVariable Long experienceId) {
     employeeService.deleteExperienceById(experienceId);
     return ResponseEntity.noContent().build();
 }

 @PostMapping("/me/experience/{experienceId}/upload-letter")
 public ResponseEntity<String> uploadMyExperienceLetter(
         @RequestParam String employeeId,
         @PathVariable Long experienceId,
         @RequestParam("file") MultipartFile file) throws Exception {
     return ResponseEntity.ok(employeeService.uploadExperienceLetter(employeeId, experienceId, file));
 }

 @GetMapping("/me/experience/{experienceId}/download-letter")
 public ResponseEntity<byte[]> downloadMyExperienceLetter(@PathVariable Long experienceId) {
     Experience exp = experienceRepository.findById(experienceId)
             .orElseThrow(() -> new RuntimeException("Experience not found"));
     byte[] data = exp.getExperienceLetter();
     return ResponseEntity.ok()
             .header(HttpHeaders.CONTENT_DISPOSITION,
                     "attachment; filename=experience_letter_" + experienceId + ".pdf")
             .contentType(MediaType.APPLICATION_PDF)
             .body(data);
 }

    
 // ------------------- Get All Employees -------------------
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        List<EmployeeDTO> dtos = employees.stream()
                                          .map(EmployeeDTO::fromEntity)
                                          .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ------------------- Get Employee By ID -------------------
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable String employeeId) {
        Employee emp = employeeService.getEmployeeById(employeeId);
        if (emp == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(EmployeeDTO.fromEntity(emp));
    }

    // ------------------- Other endpoints remain unchanged -------------------
    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable String employeeId,
            @RequestBody EmployeeDTO employeeDTO) {

        employeeDTO.setEmployeeId(employeeId); // ensure ID is set
        Employee updatedEmployee = employeeService.updateEmployee(employeeDTO);
        System.out.println("Received DTO: " + employeeDTO);

        return ResponseEntity.ok(EmployeeDTO.fromEntity(updatedEmployee));
    }



    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String employeeId) {
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }

    // ------------------- Bank Details -------------------
    @GetMapping("/{employeeId}/bank-details")
    public ResponseEntity<List<BankDetails>> getAllBankDetails(@PathVariable String employeeId) {
        return ResponseEntity.ok(employeeService.getAllBankDetails(employeeId));
    }

    @PostMapping("/{employeeId}/bank-details")
    public ResponseEntity<BankDetails> createBankDetails(
            @PathVariable String employeeId,
            @RequestBody BankDetails bankDetails) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        bankDetails.setEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.saveBankDetails(bankDetails));
    }

    @PutMapping("/{employeeId}/bank-details")
    public ResponseEntity<BankDetails> updateBankDetails(
            @PathVariable String employeeId,
            @RequestBody BankDetails bankDetails) {
        BankDetails existing = employeeService.getBankDetails(employeeId);
        existing.setAccountNumber(bankDetails.getAccountNumber());
        existing.setBankName(bankDetails.getBankName());
        existing.setIfscCode(bankDetails.getIfscCode());
        existing.setBranch(bankDetails.getBranch());
        return ResponseEntity.ok(employeeService.saveBankDetails(existing));
    }

    @DeleteMapping("/{employeeId}/bank-details")
    public ResponseEntity<Void> deleteBankDetails(@PathVariable String employeeId) {
        employeeService.deleteBankDetails(employeeId);
        return ResponseEntity.noContent().build();
    }

    // ------------------- Personal Details -------------------
    @GetMapping("/{employeeId}/personal-details")
    public ResponseEntity<PersonalDetails> getPersonalDetails(@PathVariable String employeeId) {
        return ResponseEntity.ok(employeeService.getPersonalDetails(employeeId));
    }

    @PostMapping("/{employeeId}/personal-details")
    public ResponseEntity<PersonalDetails> createPersonalDetails(
            @PathVariable String employeeId,
            @RequestBody PersonalDetails personalDetails) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        personalDetails.setEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.savePersonalDetails(personalDetails));
    }

    @PutMapping("/{employeeId}/personal-details")
    public ResponseEntity<PersonalDetails> updatePersonalDetails(
            @PathVariable String employeeId,
            @RequestBody PersonalDetails personalDetails) {
        PersonalDetails existing = employeeService.getPersonalDetails(employeeId);
        existing.setDateofBirth(personalDetails.getDateofBirth());
        existing.setGender(personalDetails.getGender());
        existing.setFatherName(personalDetails.getFatherName());
        existing.setPanNumber(personalDetails.getPanNumber());
        existing.setAadhaarNumber(personalDetails.getAadhaarNumber());
        existing.setMaritalStatus(personalDetails.getMaritalStatus());
        existing.setNationality(personalDetails.getNationality());
        return ResponseEntity.ok(employeeService.savePersonalDetails(existing));
    }

    @DeleteMapping("/{employeeId}/personal-details")
    public ResponseEntity<Void> deletePersonalDetails(@PathVariable String employeeId) {
        employeeService.deletePersonalDetails(employeeId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{employeeId}/upload/resume")
    public ResponseEntity<String> uploadResume(@PathVariable String employeeId,
                                               @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(employeeService.uploadResume(employeeId, file));
    }

    @PostMapping("/{employeeId}/upload/aadhar")
    public ResponseEntity<String> uploadAadhar(@PathVariable String employeeId,
                                               @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(employeeService.uploadAadhar(employeeId, file));
    }

    @PostMapping("/{employeeId}/upload/pan")
    public ResponseEntity<String> uploadPan(@PathVariable String employeeId,
                                            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(employeeService.uploadPan(employeeId, file));
    }

    @PostMapping("/{employeeId}/upload/profile-photo")
    public ResponseEntity<String> uploadProfilePhoto(@PathVariable String employeeId,
                                                     @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(employeeService.uploadProfilePhoto(employeeId, file));
    }
    

    // -----------------------------
    // Download endpoints
    // -----------------------------
    @GetMapping("/{employeeId}/download/resume")
    public ResponseEntity<byte[]> downloadResume(@PathVariable String employeeId) {
        byte[] data = employeeService.downloadResume(employeeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resume_" + employeeId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/{employeeId}/download/aadhar")
    public ResponseEntity<byte[]> downloadAadhar(@PathVariable String employeeId) {
        byte[] data = employeeService.downloadAadhar(employeeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=aadhar_" + employeeId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/{employeeId}/download/pan")
    public ResponseEntity<byte[]> downloadPan(@PathVariable String employeeId) {
        byte[] data = employeeService.downloadPan(employeeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pan_" + employeeId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/{employeeId}/download/profile-photo")
    public ResponseEntity<byte[]> downloadProfilePhoto(@PathVariable String employeeId) {
        byte[] data = employeeService.downloadProfilePhoto(employeeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=profile_" + employeeId + ".jpg")
                .contentType(MediaType.IMAGE_JPEG)
                .body(data);
    }

    
    
    // ------------------- Experience -------------------
    @GetMapping("/{employeeId}/experience")
    public ResponseEntity<List<Experience>> getExperienceDetails(@PathVariable String employeeId) {
        return ResponseEntity.ok(employeeService.getExperienceDetails(employeeId));
    }

    @PostMapping("/{employeeId}/experience")
    public ResponseEntity<Experience> createExperience(
            @PathVariable String employeeId,
            @RequestBody Experience experience) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        experience.setEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.saveExperience(experience));
    }

    @DeleteMapping("/{employeeId}/experience")
    public ResponseEntity<Void> deleteAllExperience(@PathVariable String employeeId) {
        employeeService.deleteAllExperience(employeeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{employeeId}/experience/{experienceId}")
    public ResponseEntity<Void> deleteExperienceById(
            @PathVariable String employeeId,
            @PathVariable Long experienceId) {
        employeeService.deleteExperienceById(experienceId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{employeeId}/experience/{experienceId}/upload-letter")
    public ResponseEntity<String> uploadExperienceLetter(
            @PathVariable String employeeId,
            @PathVariable Long experienceId,
            @RequestParam("file") MultipartFile file) throws Exception {

        return ResponseEntity.ok(employeeService.uploadExperienceLetter(employeeId, experienceId, file));
    }

    @GetMapping("/{employeeId}/experience/{experienceId}/download-letter")
    public ResponseEntity<byte[]> downloadExperienceLetter(
            @PathVariable String employeeId,
            @PathVariable Long experienceId) {

        Experience exp = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new RuntimeException("Experience not found"));

        byte[] data = exp.getExperienceLetter();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=experience_letter_" + experienceId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }


    // ------------------- Job Details -------------------
    @GetMapping("/{employeeId}/job-details")
    public ResponseEntity<JobDetails> getJobDetails(@PathVariable String employeeId) {
        return ResponseEntity.ok(employeeService.getJobDetails(employeeId));
    }

    @PostMapping("/{employeeId}/job-details")
    public ResponseEntity<JobDetails> createJobDetails(
            @PathVariable String employeeId,
            @RequestBody JobDetails jobDetails) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        jobDetails.setEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.saveJobDetails(jobDetails));
    }

    @PutMapping("/{employeeId}/job-details")
    public ResponseEntity<JobDetails> updateJobDetails(
            @PathVariable String employeeId,
            @RequestBody JobDetails jobDetails) {
        JobDetails existing = employeeService.getJobDetails(employeeId);
        existing.setDeptId(jobDetails.getDeptId());
        existing.setDepartment(jobDetails.getDepartment());
        existing.setDesignation(jobDetails.getDesignation());
        existing.setDoj(jobDetails.getDoj());
        return ResponseEntity.ok(employeeService.saveJobDetails(existing));
    }

    @DeleteMapping("/{employeeId}/job-details")
    public ResponseEntity<Void> deleteJobDetails(@PathVariable String employeeId) {
        employeeService.deleteJobDetails(employeeId);
        return ResponseEntity.noContent().build();
    }

    // ------------------- Status -------------------
    @PatchMapping("/{employeeId}/deactivate")
    public ResponseEntity<Void> deactivateEmployee(@PathVariable String employeeId) {
        employeeService.deactivateEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{employeeId}/reactivate")
    public ResponseEntity<Void> reactivateEmployee(
            @PathVariable String employeeId,
            @RequestParam String joiningDate) {
        employeeService.reactivateEmployee(employeeId, LocalDate.parse(joiningDate));
        return ResponseEntity.noContent().build();
    }

    // ------------------- Report -------------------
    @GetMapping("/report")
    public ResponseEntity<byte[]> generateEmployeeReport(
            @RequestParam(required = false) Boolean active,
            HttpServletResponse response) throws IOException {

        List<Employee> employees = active != null
                ? (active ? employeeService.getActiveEmployees() : employeeService.getInactiveEmployees())
                : employeeService.getAllEmployees();

        byte[] reportBytes = employeeService.generateEmployeeReport(employees);

        String filename = active != null
                ? (active ? "Active_Employees_Report.xlsx" : "Inactive_Employees_Report.xlsx")
                : "All_Employees_Report.xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(reportBytes);
    }
}