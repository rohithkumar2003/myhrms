package com.example.demo.repository;

import com.example.demo.model.PermissionHours;
import com.example.demo.model.PermissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PermissionHourRepository extends JpaRepository<PermissionHours, Long> {
    List<PermissionHours> findByEmployeeId(String employeeId);
    List<PermissionHours> findByStatus(PermissionStatus status);
    
    @Query("SELECT p FROM PermissionHours p WHERE p.status IN :statuses")
    List<PermissionHours> findByStatusIn(@Param("statuses") List<PermissionStatus> statuses);
    
    List<PermissionHours> findByDate(LocalDate date);
    
    // NEW: Required methods for employee-specific queries
    List<PermissionHours> findByEmployeeIdAndStatus(String employeeId, PermissionStatus status);
    List<PermissionHours> findByEmployeeIdAndDate(String employeeId, LocalDate date);
    
    @Query("SELECT p FROM PermissionHours p WHERE p.employeeId = :employeeId AND p.date BETWEEN :startDate AND :endDate")
    List<PermissionHours> findByEmployeeIdAndDateRange(@Param("employeeId") String employeeId, 
                                                     @Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate);
    
    @Query("SELECT p FROM PermissionHours p WHERE p.status = 'PENDING' ORDER BY p.requestDate DESC")
    List<PermissionHours> findPendingRequests();
    
    @Query("SELECT p FROM PermissionHours p WHERE p.actionBy = :actionBy ORDER BY p.actionDate DESC")
    List<PermissionHours> findByActionBy(@Param("actionBy") String actionBy);
}