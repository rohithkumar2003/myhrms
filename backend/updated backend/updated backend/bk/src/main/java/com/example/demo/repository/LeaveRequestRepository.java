package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.LeaveRequest;
import com.example.demo.model.LeaveRequestDay;
import com.example.demo.model.LeaveStatus;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    
    // Derived query methods
    List<LeaveRequest> findByEmployeeId(String employeeId);
    
    // Custom query methods
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId AND lr.status = :status")
    List<LeaveRequest> findByEmployeeIdAndStatus(@Param("employeeId") String employeeId, 
                                                @Param("status") LeaveStatus status);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId AND YEAR(lr.fromDate) = :year AND MONTH(lr.fromDate) = :month")
    List<LeaveRequest> findByEmployeeIdAndMonth(@Param("employeeId") String employeeId, 
                                               @Param("year") int year, 
                                               @Param("month") int month);
 // In LeaveRequestDayRepository.java - Add this method
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
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId AND lr.fromDate <= :endDate AND lr.toDate >= :startDate")
    List<LeaveRequest> findOverlappingLeaves(@Param("employeeId") String employeeId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
 // In LeaveRequestRepository.java - Add these methods
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
           "AND lr.status = 'APPROVED' " +
           "AND (:startDate IS NULL OR lr.toDate >= :startDate) " +
           "AND (:endDate IS NULL OR lr.fromDate <= :endDate) " +
           "ORDER BY lr.fromDate")
    List<LeaveRequest> findApprovedLeavesByEmployeeAndDateRange(
        @Param("employeeId") String employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
           "AND lr.toDate < :date AND lr.status = 'APPROVED' " +
           "ORDER BY lr.toDate DESC")
    Optional<LeaveRequest> findLatestLeaveBeforeDate(@Param("employeeId") String employeeId, 
                                                   @Param("date") LocalDate date);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
           "AND lr.fromDate > :date AND lr.status = 'APPROVED' " +
           "ORDER BY lr.fromDate ASC")
    Optional<LeaveRequest> findEarliestLeaveAfterDate(@Param("employeeId") String employeeId, 
                                                    @Param("date") LocalDate date);
    
    // Additional useful methods
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId AND lr.status = 'APPROVED' AND lr.fromDate <= :date AND lr.toDate >= :date")
    List<LeaveRequest> findApprovedLeavesOnDate(@Param("employeeId") String employeeId, 
                                               @Param("date") LocalDate date);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId AND lr.status = 'APPROVED' ORDER BY lr.fromDate DESC")
    List<LeaveRequest> findApprovedLeavesByEmployee(@Param("employeeId") String employeeId);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = :status")
    List<LeaveRequest> findByStatus(@Param("status") LeaveStatus status);
}