package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.EmployeeLeaveStatistics;

@Repository
public interface EmployeeLeaveStatisticsRepository extends JpaRepository<EmployeeLeaveStatistics, Long> {
    
    Optional<EmployeeLeaveStatistics> findByEmployeeIdAndYearAndMonth(String employeeId, int year, int month);
    
    List<EmployeeLeaveStatistics> findByEmployeeId(String employeeId);
    
    @Query("SELECT els FROM EmployeeLeaveStatistics els WHERE els.employeeId = :employeeId AND els.year = :year ORDER BY els.month")
    List<EmployeeLeaveStatistics> findByEmployeeIdAndYear(@Param("employeeId") String employeeId, 
                                                         @Param("year") int year);
    
    @Query("SELECT els FROM EmployeeLeaveStatistics els WHERE els.employeeId = :employeeId ORDER BY els.year DESC, els.month DESC")
    List<EmployeeLeaveStatistics> findByEmployeeIdOrderByYearMonthDesc(@Param("employeeId") String employeeId);
    
    @Query("SELECT COALESCE(SUM(els.paidLeaveCount), 0) FROM EmployeeLeaveStatistics els WHERE els.employeeId = :employeeId AND els.year = :year")
    Double getYearlyPaidLeaves(@Param("employeeId") String employeeId, 
                              @Param("year") int year);
    
    @Query("SELECT COALESCE(SUM(els.unpaidLeaveCount), 0) FROM EmployeeLeaveStatistics els WHERE els.employeeId = :employeeId AND els.year = :year")
    Double getYearlyUnpaidLeaves(@Param("employeeId") String employeeId, 
                                @Param("year") int year);
}