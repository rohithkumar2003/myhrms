package com.example.demo.repository;

import com.example.demo.model.Overtime;
import com.example.demo.model.Overtime.OTStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OvertimeRepository extends JpaRepository<Overtime, Long> {
    Optional<Overtime> findByEmployeeEmployeeIdAndDate(String employeeId, LocalDate date);
    boolean existsByEmployeeEmployeeIdAndDate(String employeeId, LocalDate date);
    
    // Add these missing methods
    @Query("SELECT o FROM Overtime o WHERE o.employee.employeeId = :employeeId")
    List<Overtime> findByEmployeeEmployeeId(@Param("employeeId") String employeeId);
    
    @Query("SELECT o FROM Overtime o WHERE o.status = :status")
    List<Overtime> findByStatus(@Param("status") OTStatus status);
    
    // Optional: Add status-specific exists method if needed
    @Query("SELECT COUNT(o) > 0 FROM Overtime o WHERE o.employee.employeeId = :employeeId AND o.date = :date AND o.status = :status")
    boolean existsByEmployeeEmployeeIdAndDateAndStatus(@Param("employeeId") String employeeId, 
                                                     @Param("date") LocalDate date,
                                                     @Param("status") OTStatus status);
 // Add to your existing OvertimeRepository interface
    List<Overtime> findByDateBetween(LocalDate startDate, LocalDate endDate);
    List<Overtime> findByEmployeeEmployeeIdAndDateBetween(String employeeId, LocalDate startDate, LocalDate endDate);
    List<Overtime> findByEmployeeEmployeeIdAndStatus(String employeeId, OTStatus status);
    List<Overtime> findByEmployeeEmployeeIdAndDateBetweenAndStatus(String employeeId, LocalDate startDate, LocalDate endDate, OTStatus status);
}
