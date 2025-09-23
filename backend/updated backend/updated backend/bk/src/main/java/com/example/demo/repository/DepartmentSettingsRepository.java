package com.example.demo.repository;

import com.example.demo.model.DepartmentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface DepartmentSettingsRepository extends JpaRepository<DepartmentSettings, Long> {
    
    // ✅ CORRECT: These methods use existing fields
    Optional<DepartmentSettings> findByDepartmentNameAndEmpType(String departmentName, String empType);
    
    @Query("SELECT ds FROM DepartmentSettings ds WHERE ds.departmentName = :departmentName AND ds.empType = 'DEFAULT'")
    Optional<DepartmentSettings> findDefaultByDepartmentName(@Param("departmentName") String departmentName);
    
    Optional<DepartmentSettings> findByDepartmentName(String departmentName);
    boolean existsByDepartmentName(String departmentName);
    
    @Query("SELECT ds FROM DepartmentSettings ds WHERE ds.departmentName = :departmentName AND (ds.empType = :empType OR ds.empType = 'DEFAULT') ORDER BY ds.empType DESC")
    Optional<DepartmentSettings> findByDepartmentNameAndEmpTypeWithFallback(@Param("departmentName") String departmentName, @Param("empType") String empType);
    
    // ❌ REMOVE THIS METHOD - departmentId field doesn't exist in the entity
    // @Query("SELECT ds FROM DepartmentSettings ds WHERE ds.departmentId = :departmentId")
    // Optional<DepartmentSettings> findByDepartmentId(@Param("departmentId") String departmentId);
}