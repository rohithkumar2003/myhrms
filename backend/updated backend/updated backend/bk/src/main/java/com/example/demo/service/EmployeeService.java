
package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.BankDetailsDTO;
import com.example.demo.dto.EmployeeDTO;
import com.example.demo.dto.EmployeeRequest;
import com.example.demo.dto.PersonalDetailsDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.BankDetails;
import com.example.demo.model.Employee;
import com.example.demo.model.Experience;
import com.example.demo.model.JobDetails;
import com.example.demo.model.PersonalDetails;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.BankDetailsRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.ExperienceRepository;
import com.example.demo.repository.JobDetailsRepository;
import com.example.demo.repository.PersonalDetailsRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ExperienceRepository experienceRepository;
    private final BankDetailsRepository bankDetailsRepository;
    private final PersonalDetailsRepository personalDetailsRepository;
    private final JobDetailsRepository jobDetailsRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // -----------------------------
    // Basic CRUD
    // -----------------------------
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<Employee> getActiveEmployees() {
        return employeeRepository.findByIsActive(true);
    }

    public List<Employee> getInactiveEmployees() {
        return employeeRepository.findByIsActive(false);
    }

    public List<String> getAllDepartments() {
        return employeeRepository.findAllCurrentDepartments();
    }

    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByCurrentDepartment(department);
    }

    public List<Employee> searchEmployees(String query) {
        return employeeRepository.searchEmployees(query);
    }

    public Employee getEmployeeById(String employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
    }

    // -----------------------------
    // Add Employee + User
    // -----------------------------
 // -----------------------------
 // Add Employee + User (Simple)
 // -----------------------------
    @Transactional
    public Employee addEmployeeSimple(EmployeeRequest request) {
        Employee employee = new Employee();

        // Auto-generate Employee ID
        String lastId = employeeRepository.findTopByOrderByEmployeeIdDesc()
                .map(Employee::getEmployeeId)
                .orElse("EMP000");
        int nextNum = Integer.parseInt(lastId.replace("EMP", "")) + 1;
        employee.setEmployeeId("EMP" + String.format("%03d", nextNum));

        // Set email if provided
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            employee.setEmail(request.getEmail());
        }

        // Save employee first
        Employee savedEmployee = employeeRepository.save(employee);

        // Create associated User
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            String rawPassword = (request.getPassword() != null && !request.getPassword().isEmpty())
                    ? request.getPassword()
                    : "default123";

            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(rawPassword)); // always encode
            user.setRole(UserRole.EMPLOYEE);
            user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
            user.setEmployee(savedEmployee);

            User savedUser = userRepository.save(user); // save user
            savedEmployee.setUser(savedUser);

            // Ensure Employee email matches User email
            if (savedEmployee.getEmail() == null) {
                savedEmployee.setEmail(savedUser.getEmail());
            }
        }

        return employeeRepository.save(savedEmployee);
    }


 // -----------------------------
 // Add Employee + User (Full)
 // -----------------------------
    @Transactional
    public Employee addEmployee(Employee employee) {
        // Generate employeeId
        if (employee.getEmployeeId() == null || employee.getEmployeeId().isEmpty()) {
            String lastId = employeeRepository.findTopByOrderByEmployeeIdDesc()
                    .map(Employee::getEmployeeId)
                    .orElse("EMP000");
            int nextNum = Integer.parseInt(lastId.replace("EMP", "")) + 1;
            employee.setEmployeeId("EMP" + String.format("%03d", nextNum));
        }

        // Link nested objects
        if (employee.getJobDetails() != null) employee.getJobDetails().setEmployee(employee);
        if (employee.getPersonalDetails() != null) employee.getPersonalDetails().setEmployee(employee);
        if (employee.getBankDetails() != null) employee.getBankDetails().setEmployee(employee);
        if (employee.getExperienceDetails() != null)
            employee.getExperienceDetails().forEach(exp -> exp.setEmployee(employee));

        // User handling
        if (employee.getUser() != null && employee.getUser().getEmail() != null) {
            User user = employee.getUser();
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists");
            }
            if (user.getPassword() == null || user.getPassword().isEmpty())
                user.setPassword("default123");
            if (user.getRole() == null) user.setRole(UserRole.EMPLOYEE);
            if (user.getEnabled() == null) user.setEnabled(true);

            // Attach user to employee (cascade saves it)
            user.setEmployee(employee);
            employee.setUser(user);
        }

        // Save Employee (User will be saved automatically)
        Employee savedEmployee = employeeRepository.save(employee);

        return savedEmployee;
    }


 public Employee findByEmail(String email) {
	    return userRepository.findByEmail(email)
	            .map(User::getEmployee)
	            .orElse(null);
	}

    public User createUserForEmployee(Employee employee, String email, String password) {
        if (email == null || email.isEmpty())return null;
        if (password == null || password.isEmpty()) password = "default123";
        
        if (employee.getUser() != null) return employee.getUser();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists: " + email);
        }
        
        if (password == null || password.isEmpty()) {
            password = "default123";  // fallback
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.EMPLOYEE);
        user.setEmployee(employee);
        user.setEnabled(true);
        userRepository.save(user);

        employee.setUser(user);

        return user;
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // -----------------------------
    // Delete Employee
    // -----------------------------
    public void deleteEmployee(String employeeId) {
        Employee employee = getEmployeeById(employeeId);
        if (employee.getUser() != null) userRepository.delete(employee.getUser());
        employeeRepository.delete(employee);
    }

    public String getEmployeeEmail(String employeeId) {
        Employee employee = getEmployeeById(employeeId);
        if (employee.getUser() != null) return employee.getUser().getEmail();
        return null;
    }
    // -----------------------------
    // Update Employee
    // -----------------------------
   
   
   
    @Transactional
    public Employee updateEmployee(EmployeeDTO employeeDTO) {
        // 1️⃣ Fetch existing employee
        Employee existingEmployee = getEmployeeById(employeeDTO.getEmployeeId());

        // 2️⃣ Update basic fields
        existingEmployee.setName(employeeDTO.getName());
        existingEmployee.setPhone(employeeDTO.getPhone());
        existingEmployee.setAddress(employeeDTO.getAddress());
        existingEmployee.setEmail(employeeDTO.getEmail());
        existingEmployee.setEmergencyContactName(employeeDTO.getEmergencyContactName());
        existingEmployee.setEmergencyContactPhone(employeeDTO.getEmergencyContactPhone());
        existingEmployee.setEmergencyContactRelation(employeeDTO.getEmergencyContactRelation());
        existingEmployee.setIsActive(employeeDTO.getIsActive());
        existingEmployee.setTypeOfEmployee(employeeDTO.getTypeOfEmployee());

        // 3️⃣ Update personalDetails
        if (employeeDTO.getPersonalDetails() != null) {
            PersonalDetailsDTO pdDTO = employeeDTO.getPersonalDetails();
            PersonalDetails pd = existingEmployee.getPersonalDetails();
            if (pd == null) {
                pd = pdDTO.toEntity();
                pd.setEmployee(existingEmployee);
                existingEmployee.setPersonalDetails(pd);
            } else {
                // overwrite all fields from DTO, even if existing is null
                pd.setDateofBirth(pdDTO.getDob());
                pd.setGender(pdDTO.getGender());
                pd.setMaritalStatus(pdDTO.getMaritalStatus());
                pd.setNationality(pdDTO.getNationality());
                pd.setFatherName(pdDTO.getFatherName());
                pd.setPanNumber(pdDTO.getPanNumber());
                pd.setAadhaarNumber(pdDTO.getAadhaarNumber());
                pd.setProfilePhoto(pdDTO.getProfilePhoto() != null
                        ? Base64.getDecoder().decode(pdDTO.getProfilePhoto())
                        : pd.getProfilePhoto());
                pd.setResume(pdDTO.getResume() != null
                        ? Base64.getDecoder().decode(pdDTO.getResume())
                        : pd.getResume());
            }
        }

        // 4️⃣ Update bankDetails
        if (employeeDTO.getBankDetails() != null) {
            BankDetailsDTO bdDTO = employeeDTO.getBankDetails();
            BankDetails bd = existingEmployee.getBankDetails();
            if (bd == null) {
                bd = bdDTO.toEntity();
                bd.setEmployee(existingEmployee);
                existingEmployee.setBankDetails(bd);
            } else {
                bd.setAccountNumber(bdDTO.getAccountNumber());
                bd.setBankName(bdDTO.getBankName());
                bd.setIfscCode(bdDTO.getIfscCode());
            }
        }

        // 5️⃣ Update experience list
        if (employeeDTO.getExperienceDetails() != null) {
            List<Experience> exps = employeeDTO.getExperienceDetails().stream()
                    .map(expDTO -> {
                        Experience e = expDTO.toEntity();
                        e.setEmployee(existingEmployee);
                        return e;
                    }).collect(Collectors.toList());
            existingEmployee.setExperienceDetails(exps);
        }

        // 6️⃣ Save employee
        return employeeRepository.save(existingEmployee);
    }


    private void updatePersonalDetails(Employee employee, EmployeeDTO employeeDTO) {
        if (employeeDTO.getPersonalDetails() != null) {
            PersonalDetailsDTO pdDTO = employeeDTO.getPersonalDetails();
            PersonalDetails existing = employee.getPersonalDetails();

            if (existing == null) {
                PersonalDetails pd = pdDTO.toEntity();
                pd.setEmployee(employee);
                employee.setPersonalDetails(pd);
            } else {
                if (pdDTO.getDob() != null) existing.setDateofBirth(pdDTO.getDob());
                if (pdDTO.getGender() != null) existing.setGender(pdDTO.getGender());
                if (pdDTO.getMaritalStatus() != null) existing.setMaritalStatus(pdDTO.getMaritalStatus());
                if (pdDTO.getNationality() != null) existing.setNationality(pdDTO.getNationality());
                if (pdDTO.getFatherName() != null) existing.setFatherName(pdDTO.getFatherName());
                if (pdDTO.getPanNumber() != null) existing.setPanNumber(pdDTO.getPanNumber());
                if (pdDTO.getAadhaarNumber() != null) existing.setAadhaarNumber(pdDTO.getAadhaarNumber());
                if (pdDTO.getProfilePhoto() != null)
                    existing.setProfilePhoto(Base64.getDecoder().decode(pdDTO.getProfilePhoto()));
                if (pdDTO.getResume() != null)
                    existing.setResume(Base64.getDecoder().decode(pdDTO.getResume()));
            }
        }
    }
    private void updateBankDetails(Employee employee, Employee employeeDetails) {
        if (employeeDetails.getBankDetails() != null) {
            BankDetails newBank = employeeDetails.getBankDetails();
            if (employee.getBankDetails() == null) {
                employee.setBankDetails(new BankDetails());
                employee.getBankDetails().setEmployee(employee);
            }
            employee.getBankDetails().setBankName(newBank.getBankName());
            employee.getBankDetails().setAccountNumber(newBank.getAccountNumber());
            employee.getBankDetails().setIfscCode(newBank.getIfscCode());
            employee.getBankDetails().setBranch(newBank.getBranch());
        }
    }

    private void updateJobDetails(Employee employee, Employee employeeDetails) {
        if (employeeDetails.getJobDetails() != null) {
            JobDetails newJob = employeeDetails.getJobDetails();
            if (employee.getJobDetails() == null) {
                employee.setJobDetails(new JobDetails());
                employee.getJobDetails().setEmployee(employee);
            }
            employee.getJobDetails().setDesignation(newJob.getDesignation());
            employee.getJobDetails().setDepartment(newJob.getDepartment());
            employee.getJobDetails().setJoiningDate(newJob.getJoiningDate());
            employee.getJobDetails().setDeptId(newJob.getDeptId());
        }
    }

//    private void updateExperienceDetails(String employeeId, Employee employee, Employee employeeDetails) {
//        if (employeeDetails.getExperienceDetails() != null) {
//            experienceRepository.deleteAll(experienceRepository.findByEmployeeEmployeeId(employeeId));
//            employee.getExperienceDetails().clear();
//            for (Experience exp : employeeDetails.getExperienceDetails()) {
//                exp.setEmployee(employee);
//                employee.getExperienceDetails().add(exp);
//            }
//        }
//    }
    @Transactional
    public void updateEmployeeExperience(String employeeId, List<Experience> updatedExperiences) {
        Employee employee = getEmployeeById(employeeId);

        for (Experience exp : updatedExperiences) {
            if (exp.getId() != null) {
                // ✅ Existing experience → update
                Experience existingExp = experienceRepository.findById(exp.getId())
                        .orElseThrow(() -> new RuntimeException("Experience not found: " + exp.getId()));

                existingExp.setCompany(exp.getCompany());
                existingExp.setDepartment(exp.getDepartment());
                existingExp.setRole(exp.getRole());
                existingExp.setSalary(exp.getSalary());
                existingExp.setJoiningDate(exp.getJoiningDate());
                existingExp.setLastWorkingDate(exp.getLastWorkingDate());
                existingExp.setPastEmploymentType(exp.getPastEmploymentType());
                existingExp.setReason(exp.getReason());

                experienceRepository.save(existingExp);

            } else {
                // ✅ New experience → add
                exp.setEmployee(employee);
                experienceRepository.save(exp);
            }
        }
    }


    // -----------------------------
    // Bank Details
    // -----------------------------
    public List<BankDetails> getAllBankDetails(String employeeId) {
        return bankDetailsRepository.findByEmployeeEmployeeId(employeeId);
    }

    public BankDetails getBankDetails(String employeeId) {
        return bankDetailsRepository.findLatestByEmployeeEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank details not found for employee: " + employeeId));
    }

    public BankDetails getFirstBankDetails(String employeeId) {
        List<BankDetails> list = bankDetailsRepository.findByEmployeeEmployeeId(employeeId);
        if (list.isEmpty()) throw new ResourceNotFoundException("Bank details not found for employee: " + employeeId);
        return list.get(0);
    }

    public BankDetails saveBankDetails(BankDetails bankDetails) {
        return bankDetailsRepository.save(bankDetails);
    }

    public void deleteBankDetails(String employeeId) {
        bankDetailsRepository.deleteByEmployeeEmployeeId(employeeId);
    }

    public boolean hasBankDetails(String employeeId) {
        return bankDetailsRepository.existsByEmployeeEmployeeId(employeeId);
    }

    // -----------------------------
    // Personal Details
    // -----------------------------
    public PersonalDetails getPersonalDetails(String employeeId) {
        return personalDetailsRepository.findByEmployeeEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal details not found for employee: " + employeeId));
    }

    public PersonalDetails savePersonalDetails(PersonalDetails personalDetails) {
        return personalDetailsRepository.save(personalDetails);
    }

    public void deletePersonalDetails(String employeeId) {
        personalDetailsRepository.deleteByEmployeeEmployeeId(employeeId);
    }

    public boolean hasPersonalDetails(String employeeId) {
        return personalDetailsRepository.existsByEmployeeEmployeeId(employeeId);
    }
    

    public String uploadResume(String employeeId, MultipartFile file) throws IOException {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        PersonalDetails personalDetails = employee.getPersonalDetails();
        personalDetails.setResume(file.getBytes());
        personalDetailsRepository.save(personalDetails);

        return "Resume uploaded successfully for " + employeeId;
    }

    // Upload Aadhaar
    public String uploadAadhar(String employeeId, MultipartFile file) throws IOException {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        PersonalDetails personalDetails = employee.getPersonalDetails();
        personalDetails.setAadhar(file.getBytes());
        personalDetailsRepository.save(personalDetails);

        return "Aadhaar uploaded successfully for " + employeeId;
    }

    // Upload PAN
    public String uploadPan(String employeeId, MultipartFile file) throws IOException {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        PersonalDetails personalDetails = employee.getPersonalDetails();
        personalDetails.setPan(file.getBytes());
        personalDetailsRepository.save(personalDetails);

        return "PAN uploaded successfully for " + employeeId;
    }

    // Upload Profile Photo
    public String uploadProfilePhoto(String employeeId, MultipartFile file) throws IOException {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        PersonalDetails personalDetails = employee.getPersonalDetails();
        personalDetails.setProfilePhoto(file.getBytes());
        personalDetailsRepository.save(personalDetails);

        return "Profile photo uploaded successfully for " + employeeId;
    }
    public String uploadExperienceLetter(String employeeId, Long experienceId, MultipartFile file) throws Exception {
        Experience exp = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new RuntimeException("Experience not found"));

        exp.setExperienceLetter(file.getBytes());
        experienceRepository.save(exp);

        return "Experience letter uploaded successfully for experience ID " + experienceId;
    }

    
    

    // Download Resume
    public byte[] downloadResume(String employeeId) {
        return getFile(employeeId).getResume();
    }

    // Download Aadhaar
    public byte[] downloadAadhar(String employeeId) {
        return getFile(employeeId).getAadhar();
    }

    // Download PAN
    public byte[] downloadPan(String employeeId) {
        return getFile(employeeId).getPan();
    }

    // Download Profile Photo
    public byte[] downloadProfilePhoto(String employeeId) {
        return getFile(employeeId).getProfilePhoto();
    }

    // Helper
    private PersonalDetails getFile(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return employee.getPersonalDetails();
    }



    // -----------------------------
    // Experience
    // -----------------------------
    public List<Experience> getExperienceDetails(String employeeId) {
        return experienceRepository.findByEmployeeEmployeeId(employeeId);
    }

    public Experience saveExperience(Experience experience) {
        return experienceRepository.save(experience);
    }

    public void deleteAllExperience(String employeeId) {
        experienceRepository.deleteByEmployeeEmployeeId(employeeId);
    }

    public void deleteExperienceById(Long experienceId) {
        experienceRepository.deleteById(experienceId);
    }

    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public boolean hasExperience(String employeeId) {
        return experienceRepository.existsByEmployeeEmployeeId(employeeId);
    }

    // -----------------------------
    // Job Details
    // -----------------------------
    public JobDetails getJobDetails(String employeeId) {
        return jobDetailsRepository.findByEmployeeEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Job details not found for employee: " + employeeId));
    }

    public JobDetails saveJobDetails(JobDetails jobDetails) {
        return jobDetailsRepository.save(jobDetails);
    }

    public void deleteJobDetails(String employeeId) {
        jobDetailsRepository.deleteByEmployeeEmployeeId(employeeId);
    }

    public boolean hasJobDetails(String employeeId) {
        return jobDetailsRepository.existsByEmployeeEmployeeId(employeeId);
    }

    // -----------------------------
    // Status management
    // -----------------------------
    public void deactivateEmployee(String employeeId) {
        Employee employee = getEmployeeById(employeeId);
        employee.setIsActive(false);

        experienceRepository.findCurrentExperience(employeeId)
                .ifPresent(exp -> exp.setLastWorkingDate(LocalDate.now().toString()));

        employeeRepository.save(employee);
    }

    public void reactivateEmployee(String employeeId, LocalDate joiningDate) {
        Employee employee = getEmployeeById(employeeId);
        employee.setIsActive(true);

        Experience lastExp = experienceRepository.findByEmployeeEmployeeIdOrderByIdDesc(employeeId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No experience found for employee: " + employeeId));

        Experience newExp = Experience.builder()
                .employee(employee)
                .company("Your Company Name")
                .department(lastExp.getDepartment())
                .role(lastExp.getRole())
                .salary(lastExp.getSalary())
                .joiningDate(joiningDate)
                .lastWorkingDate("Present")
                .build();

        employee.getExperienceDetails().add(newExp);
        employeeRepository.save(employee);
    }

    // -----------------------------
    // Excel report
    // -----------------------------
    public byte[] generateEmployeeReport(List<Employee> employees) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Employees");

            Row headerRow = sheet.createRow(0);
            String[] headers = { "Employee ID", "Full Name", "Email Address", "Phone Number",
                    "Current Department", "Current Role", "Joining Date", "Current Salary" };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Employee emp : employees) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(emp.getEmployeeId());
                row.createCell(1).setCellValue(emp.getName());
                row.createCell(2).setCellValue(emp.getUser() != null ? emp.getUser().getEmail() : "");
                row.createCell(3).setCellValue(emp.getPhone());

                Optional<Experience> currentExp = experienceRepository.findCurrentExperience(emp.getEmployeeId());
                if (currentExp.isPresent()) {
                    Experience exp = currentExp.get();
                    row.createCell(4).setCellValue(exp.getDepartment());
                    row.createCell(5).setCellValue(exp.getRole());
                    row.createCell(6).setCellValue(exp.getJoiningDate().toString());
                    if (exp.getSalary() != null) row.createCell(7).setCellValue(exp.getSalary().doubleValue());
                }
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
