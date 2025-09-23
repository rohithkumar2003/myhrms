package com.example.demo.dto;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.model.BankDetails;
// Use this instead of jakarta
import com.example.demo.model.Employee;
import com.example.demo.model.Employee.EmployeeType;
import com.example.demo.model.Experience;
import com.example.demo.model.PersonalDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private String employeeId;
    private String name;
 // in EmployeeDTO
    private String email; // add this

    private String phone;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private Boolean isActive;
    
    private PersonalDetailsDTO personalDetails;
    private BankDetailsDTO bankDetails;
    private List<ExperienceDTO> experienceDetails;
    
    
    private EmployeeType typeOfEmployee;  // Add this field
    
    // Convert from Entity to DTO
    public static EmployeeDTO fromEntity(Employee employee) {
        return EmployeeDTO.builder()
                .employeeId(employee.getEmployeeId())
                .name(employee.getName())
                .phone(employee.getPhone())
                .address(employee.getAddress())
                .email(employee.getEmail() != null ? employee.getEmail() 
                        : (employee.getUser() != null ? employee.getUser().getEmail() : null))

                .typeOfEmployee(employee.getTypeOfEmployee())  
                .emergencyContactName(employee.getEmergencyContactName())
                .emergencyContactPhone(employee.getEmergencyContactPhone() != null 
                        ? employee.getEmergencyContactPhone().toString() : null)
                .emergencyContactRelation(employee.getEmergencyContactRelation())
                .isActive(employee.getIsActive())
                .personalDetails(PersonalDetailsDTO.fromEntity(employee.getPersonalDetails()))
                .bankDetails(BankDetailsDTO.fromEntity(employee.getBankDetails()))
                .experienceDetails(employee.getExperienceDetails() != null ? 
                        employee.getExperienceDetails().stream()
                                .map(ExperienceDTO::fromEntity)
                                .collect(Collectors.toList()) : null)
                // Add this line
                .build();
    }
    
    // Convert to Entity from DTO
 // ✅ Convert DTO → Entity
 // ✅ Convert DTO → Entity
    public Employee toEntity(Employee existingEmployee) {
        Employee employee = existingEmployee != null ? existingEmployee : new Employee();

        // Basic fields (can be safely overwritten since these usually come every time)
        employee.setEmployeeId(this.employeeId);
        if (this.name != null) employee.setName(this.name);
        if (this.phone != null) employee.setPhone(this.phone);
        if (this.address != null) employee.setAddress(this.address);
        if (this.email != null) employee.setEmail(this.email);
        if (this.emergencyContactName != null) employee.setEmergencyContactName(this.emergencyContactName);
        if (this.emergencyContactPhone != null) employee.setEmergencyContactPhone(this.emergencyContactPhone);
        if (this.emergencyContactRelation != null) employee.setEmergencyContactRelation(this.emergencyContactRelation);
        if (this.isActive != null) employee.setIsActive(this.isActive);
        if (this.typeOfEmployee != null) employee.setTypeOfEmployee(this.typeOfEmployee);

        // ✅ PersonalDetails (merge instead of overwrite)
        if (this.personalDetails != null) {
            PersonalDetailsDTO pdDTO = this.personalDetails;
            PersonalDetails pd = employee.getPersonalDetails();

            if (pd == null) {
                pd = pdDTO.toEntity();
                pd.setEmployee(employee);
                employee.setPersonalDetails(pd);
            } else {
                if (pdDTO.getDob() != null) pd.setDateofBirth(pdDTO.getDob());
                if (pdDTO.getGender() != null) pd.setGender(pdDTO.getGender());
                if (pdDTO.getMaritalStatus() != null) pd.setMaritalStatus(pdDTO.getMaritalStatus());
                if (pdDTO.getNationality() != null) pd.setNationality(pdDTO.getNationality());
                if (pdDTO.getFatherName() != null) pd.setFatherName(pdDTO.getFatherName());
                if (pdDTO.getPanNumber() != null) pd.setPanNumber(pdDTO.getPanNumber());
                if (pdDTO.getAadhaarNumber() != null) pd.setAadhaarNumber(pdDTO.getAadhaarNumber());

                if (pdDTO.getProfilePhoto() != null && !pdDTO.getProfilePhoto().isEmpty()) {
                    pd.setProfilePhoto(Base64.getDecoder().decode(pdDTO.getProfilePhoto()));
                }
                if (pdDTO.getResume() != null && !pdDTO.getResume().isEmpty()) {
                    pd.setResume(Base64.getDecoder().decode(pdDTO.getResume()));
                }
            }
        }

        // ✅ BankDetails (merge instead of overwrite)
        if (this.bankDetails != null) {
            BankDetailsDTO bdDTO = this.bankDetails;
            BankDetails bd = employee.getBankDetails();

            if (bd == null) {
                bd = bdDTO.toEntity();
                bd.setEmployee(employee);
                employee.setBankDetails(bd);
            } else {
                if (bdDTO.getAccountNumber() != null) bd.setAccountNumber(bdDTO.getAccountNumber());
                if (bdDTO.getBankName() != null) bd.setBankName(bdDTO.getBankName());
                if (bdDTO.getIfscCode() != null) bd.setIfscCode(bdDTO.getIfscCode());
            }
        }

        // ✅ Experience (replace list only if DTO provides it)
        if (this.experienceDetails != null) {
            List<Experience> exps = this.experienceDetails.stream()
                    .map(expDTO -> {
                        Experience e = expDTO.toEntity();
                        e.setEmployee(employee);
                        return e;
                    }).collect(Collectors.toList());
            employee.setExperienceDetails(exps);
        }

        return employee;
    }

}