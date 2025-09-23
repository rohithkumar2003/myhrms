package com.example.demo.repository;

import com.example.demo.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeEmployeeIdAndDate(String employeeId, LocalDate date);
    List<Attendance> findByEmployeeEmployeeIdAndDateBetween(String employeeId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.employee.employeeId = :employeeId AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date")
    List<Attendance> findAttendanceHistory(@Param("employeeId") String employeeId, 
                                         @Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
	List<Attendance> findByDateAndPunchOutTimeIsNull(LocalDate today);
}