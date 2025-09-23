package com.example.demo.dto;

import java.time.LocalDate;
import java.util.Base64;

import com.example.demo.model.PersonalDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalDetailsDTO {
    private Long id;
    private LocalDate dob;
    private String gender;
    private String maritalStatus;
    private String nationality;
    private String fatherName;
    private String panNumber;
    private String aadhaarNumber;

    // Base64 strings for frontend
    private String profilePhoto;
    private String resume;
    private String aadhar;  // optional
    private String pan;     // optional

    // ✅ From Entity → DTO
    public static PersonalDetailsDTO fromEntity(PersonalDetails personalDetails) {
        if (personalDetails == null) return null;

        return PersonalDetailsDTO.builder()
                .id(personalDetails.getId())
                .dob(personalDetails.getDateofBirth())
                .gender(personalDetails.getGender())
                .maritalStatus(personalDetails.getMaritalStatus())
                .nationality(personalDetails.getNationality())
                .fatherName(personalDetails.getFatherName())
                .panNumber(personalDetails.getPanNumber())
                .aadhaarNumber(personalDetails.getAadhaarNumber())
                .profilePhoto(personalDetails.getProfilePhoto() != null
                        ? Base64.getEncoder().encodeToString(personalDetails.getProfilePhoto()) : null)
                .resume(personalDetails.getResume() != null
                        ? Base64.getEncoder().encodeToString(personalDetails.getResume()) : null)
                .aadhar(personalDetails.getAadhar() != null
                        ? Base64.getEncoder().encodeToString(personalDetails.getAadhar()) : null)
                .pan(personalDetails.getPan() != null
                        ? Base64.getEncoder().encodeToString(personalDetails.getPan()) : null)
                .build();
    }

    // ✅ DTO → New Entity
    public PersonalDetails toEntity() {
        return PersonalDetails.builder()
                .id(this.id)
                .dateofBirth(this.dob)
                .gender(this.gender)
                .maritalStatus(this.maritalStatus)
                .nationality(this.nationality)
                .fatherName(this.fatherName)
                .panNumber(this.panNumber)
                .aadhaarNumber(this.aadhaarNumber)
                .profilePhoto(this.profilePhoto != null ? Base64.getDecoder().decode(this.profilePhoto) : null)
                .resume(this.resume != null ? Base64.getDecoder().decode(this.resume) : null)
                .aadhar(this.aadhar != null ? Base64.getDecoder().decode(this.aadhar) : null)
                .pan(this.pan != null ? Base64.getDecoder().decode(this.pan) : null)
                .build();
    }

    // ✅ DTO → Update existing Entity
    public PersonalDetails toEntity(PersonalDetails existing) {
        if (existing == null) {
            existing = new PersonalDetails();
        }

        if (this.id != null) existing.setId(this.id);
        if (this.dob != null) existing.setDateofBirth(this.dob);
        if (this.gender != null) existing.setGender(this.gender);
        if (this.maritalStatus != null) existing.setMaritalStatus(this.maritalStatus);
        if (this.nationality != null) existing.setNationality(this.nationality);
        if (this.fatherName != null) existing.setFatherName(this.fatherName);
        if (this.panNumber != null) existing.setPanNumber(this.panNumber);
        if (this.aadhaarNumber != null) existing.setAadhaarNumber(this.aadhaarNumber);

        if (this.profilePhoto != null && !this.profilePhoto.isEmpty())
            existing.setProfilePhoto(Base64.getDecoder().decode(this.profilePhoto));
        if (this.resume != null && !this.resume.isEmpty())
            existing.setResume(Base64.getDecoder().decode(this.resume));
        if (this.aadhar != null && !this.aadhar.isEmpty())
            existing.setAadhar(Base64.getDecoder().decode(this.aadhar));
        if (this.pan != null && !this.pan.isEmpty())
            existing.setPan(Base64.getDecoder().decode(this.pan));

        return existing;
    }
}
