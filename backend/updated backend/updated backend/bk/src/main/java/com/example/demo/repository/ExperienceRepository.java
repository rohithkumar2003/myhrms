package com.example.demo.repository;

import com.example.demo.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    
    @Query("SELECT e.department FROM Experience e WHERE e.employee.employeeId = :employeeId AND e.lastWorkingDate = 'Present' ORDER BY e.joiningDate DESC LIMIT 1")
    Optional<String> findCurrentDepartmentByEmployeeId(@Param("employeeId") String employeeId);
    
    // Method to find current experience
    @Query("SELECT e FROM Experience e WHERE e.employee.employeeId = :employeeId AND e.lastWorkingDate = 'Present'")
    Optional<Experience> findCurrentExperience(@Param("employeeId") String employeeId);
    
    // Method to find most recent experience for an employee
    @Query("SELECT e FROM Experience e WHERE e.employee.employeeId = :employeeId ORDER BY e.id DESC")
    List<Experience> findByEmployeeEmployeeIdOrderByIdDesc(@Param("employeeId") String employeeId);
    
    // Default method to get the top result
    default Optional<Experience> findTopByEmployeeEmployeeIdOrderByIdDesc(String employeeId) {
        List<Experience> experiences = findByEmployeeEmployeeIdOrderByIdDesc(employeeId);
        return experiences.isEmpty() ? Optional.empty() : Optional.of(experiences.get(0));
    }
    
    // Additional required methods
    List<Experience> findByEmployeeEmployeeId(String employeeId);
    
    void deleteByEmployeeEmployeeId(String employeeId);
    
    boolean existsByEmployeeEmployeeId(String employeeId);
    
    // Find all experiences for an employee ordered by joining date (newest first)
    @Query("SELECT e FROM Experience e WHERE e.employee.employeeId = :employeeId ORDER BY e.joiningDate DESC")
    List<Experience> findByEmployeeEmployeeIdOrderByJoiningDateDesc(@Param("employeeId") String employeeId);
    
    // Find experiences by company name
    @Query("SELECT e FROM Experience e WHERE e.employee.employeeId = :employeeId AND LOWER(e.company) LIKE LOWER(CONCAT('%', :company, '%'))")
    List<Experience> findByEmployeeEmployeeIdAndCompanyContainingIgnoreCase(@Param("employeeId") String employeeId, @Param("company") String company);
    
    // Find experiences by role
    @Query("SELECT e FROM Experience e WHERE e.employee.employeeId = :employeeId AND LOWER(e.role) LIKE LOWER(CONCAT('%', :role, '%'))")
    List<Experience> findByEmployeeEmployeeIdAndRoleContainingIgnoreCase(@Param("employeeId") String employeeId, @Param("role") String role);
    
    // Find experiences by department
    @Query("SELECT e FROM Experience e WHERE e.employee.employeeId = :employeeId AND LOWER(e.department) LIKE LOWER(CONCAT('%', :department, '%'))")
    List<Experience> findByEmployeeEmployeeIdAndDepartmentContainingIgnoreCase(@Param("employeeId") String employeeId, @Param("department") String department);
    
    // Count total experiences for an employee
    @Query("SELECT COUNT(e) FROM Experience e WHERE e.employee.employeeId = :employeeId")
    Long countByEmployeeEmployeeId(@Param("employeeId") String employeeId);
}