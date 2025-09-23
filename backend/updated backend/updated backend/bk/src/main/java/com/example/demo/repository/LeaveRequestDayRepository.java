package com.example.demo.repository;

import com.example.demo.model.LeaveRequestDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestDayRepository extends JpaRepository<LeaveRequestDay, Long> {
	// Add this method to your LeaveRequestDayRepository
	@Query("SELECT lrd FROM LeaveRequestDay lrd " +
	       "JOIN lrd.leaveRequest lr " +
	       "WHERE lr.employeeId = :employeeId " +
	       "AND lrd.leaveDate BETWEEN :startDate AND :endDate " +
	       "AND lrd.sandwichFlag = true " +
	       "AND lr.status = 'APPROVED'")
	List<LeaveRequestDay> findSandwichLeavesByEmployeeAndDateRange(
	    @Param("employeeId") String employeeId,
	    @Param("startDate") LocalDate startDate,
	    @Param("endDate") LocalDate endDate);
    List<LeaveRequestDay> findByLeaveRequestId(Long leaveRequestId);
    
    @Query("SELECT lrd FROM LeaveRequestDay lrd WHERE lrd.leaveRequest.employeeId = :employeeId AND lrd.leaveDate = :date")
    List<LeaveRequestDay> findByEmployeeIdAndDate(@Param("employeeId") String employeeId, 
                                                 @Param("date") LocalDate date);
    
    @Query("SELECT lrd FROM LeaveRequestDay lrd WHERE lrd.leaveRequest.employeeId = :employeeId AND lrd.leaveDate BETWEEN :start AND :end")
    List<LeaveRequestDay> findByEmployeeIdAndDateRange(@Param("employeeId") String employeeId, 
                                                      @Param("start") LocalDate start, 
                                                      @Param("end") LocalDate end);
    
    @Query("SELECT lrd FROM LeaveRequestDay lrd WHERE lrd.leaveRequest.employeeId = :employeeId AND lrd.sandwichFlag = true AND lrd.leaveDate BETWEEN :start AND :end")
    List<LeaveRequestDay> findSandwichLeavesByEmployeeIdAndDateRange(@Param("employeeId") String employeeId, 
                                                                    @Param("start") LocalDate start,
                                                                    @Param("end") LocalDate end);
    
    @Query("SELECT lrd FROM LeaveRequestDay lrd WHERE lrd.leaveRequest.employeeId = :employeeId AND lrd.leaveDate = :date AND lrd.sandwichFlag = true")
    List<LeaveRequestDay> findSandwichLeavesByEmployeeIdAndDate(@Param("employeeId") String employeeId, 
                                                               @Param("date") LocalDate date);
    
    @Query("SELECT lrd FROM LeaveRequestDay lrd WHERE lrd.leaveRequest.employeeId = :employeeId AND lrd.leaveCategory = 'PAID' AND lrd.leaveDate BETWEEN :start AND :end")
    List<LeaveRequestDay> findPaidLeavesByEmployeeIdAndDateRange(@Param("employeeId") String employeeId, 
                                                                @Param("start") LocalDate start, 
                                                                @Param("end") LocalDate end);
    
    // Add this method for counting paid leaves - FIXED SYNTAX
    @Query("SELECT COUNT(lrd) FROM LeaveRequestDay lrd " +
           "WHERE lrd.leaveRequest.employeeId = :employeeId " +
           "AND YEAR(lrd.leaveDate) = :year " +
           "AND MONTH(lrd.leaveDate) = :month " +
           "AND lrd.leaveCategory = 'PAID'")
    long countPaidLeavesByEmployeeAndMonth(@Param("employeeId") String employeeId,
                                          @Param("year") int year,
                                          @Param("month") int month);
}