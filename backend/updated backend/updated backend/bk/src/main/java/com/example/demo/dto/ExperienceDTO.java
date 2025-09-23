package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.demo.model.Experience;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceDTO {
	  private Long id;
    private String company;
    private String role;
    private String department;
    private LocalDate joiningDate;
    private String lastWorkingDate;
    private BigDecimal salary;
    private Integer years;
    
    public static ExperienceDTO fromEntity(Experience experience) {
        if (experience == null) return null;
        
        return ExperienceDTO.builder()
        	    .id(experience.getId())
                .company(experience.getCompany())
                .role(experience.getRole())
                .department(experience.getDepartment())
                .joiningDate(experience.getJoiningDate())
                .lastWorkingDate(experience.getLastWorkingDate())
                .salary(experience.getSalary())
                .years(experience.getYears())
                .build();
    }
    
    public Experience toEntity() {
        return Experience.builder()
        		 .id(this.id)
                .company(this.company)
                .role(this.role)
                .department(this.department)
                .joiningDate(this.joiningDate)
                .lastWorkingDate(this.lastWorkingDate)
                .salary(this.salary)
                .years(this.years)
                .build();
    }
}