package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.EmployeeLeaveStatistics;
import com.example.demo.model.Holiday;
import com.example.demo.model.LeaveCategory;
import com.example.demo.model.LeaveDayType;
import com.example.demo.model.LeaveRequest;
import com.example.demo.model.LeaveRequestDay;
import com.example.demo.model.LeaveStatus;
import com.example.demo.model.LeaveType;
import com.example.demo.repository.EmployeeLeaveStatisticsRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.LeaveRequestDayRepository;
import com.example.demo.repository.LeaveRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestDayRepository leaveRequestDayRepository;
    private final EmployeeLeaveStatisticsRepository employeeLeaveStatisticsRepository;
    private final HolidayService holidayService;
    private final EmployeeRepository employeeRepository; // Added for employee validation
    
    public LeaveRequest applyForLeave(LeaveRequest leaveRequest) {
        // Validate employee exists
        validateEmployeeExists(leaveRequest.getEmployeeId());
        
        // Validate leave request
        validateLeaveRequest(leaveRequest);
        
        // Check for overlapping leaves
        checkForOverlappingLeaves(leaveRequest);
        
     // Calculate leave days
        int leaveDays = calculateLeaveDays(leaveRequest.getFromDate(), leaveRequest.getToDate());
        leaveRequest.setLeaveDays(leaveDays);
        
        // Apply the one paid leave per month rule
        applyPaidLeaveRule(leaveRequest);
        
        // Save the leave request
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        // Create leave request days
        createLeaveRequestDays(savedRequest);
        
        // Check for sandwich leaves AFTER creating the days
        detectAndMarkSandwichLeaves(savedRequest);
        
        // Update employee statistics
        updateEmployeeStatistics(savedRequest);
        
        return savedRequest;
    }
    
    private void validateEmployeeExists(String employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new IllegalArgumentException("Employee not found with id: " + employeeId);
        }
    }
    private int calculateLeaveDays(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            return 0;
        }
        
        int days = 0;
        LocalDate currentDate = fromDate;
        
        while (!currentDate.isAfter(toDate)) {
            // Skip weekends and holidays if needed
            // For now, just count all days
            days++;
            currentDate = currentDate.plusDays(1);
        }
        
        return days;
    }
    
    private void validateLeaveRequest(LeaveRequest leaveRequest) {
        if (leaveRequest.getFromDate().isAfter(leaveRequest.getToDate())) {
            throw new IllegalArgumentException("From date cannot be after to date");
        }
        
        //if (leaveRequest.getFromDate().isBefore(LocalDate.now())) {
         //   throw new IllegalArgumentException("Cannot apply for leave in the past");
    //    }
        
        if (leaveRequest.getFromDate().equals(leaveRequest.getToDate())) {
            if (leaveRequest.getLeaveDayType() == LeaveDayType.HALF_DAY && 
                leaveRequest.getHalfDaySession() == null) {
                throw new IllegalArgumentException("Half day session must be specified for half day leaves");
            }
        } else {
            if (leaveRequest.getLeaveDayType() == LeaveDayType.HALF_DAY) {
                throw new IllegalArgumentException("Half day leaves can only be applied for single days");
            }
        }
    }
    
    private void checkForOverlappingLeaves(LeaveRequest leaveRequest) {
        List<LeaveRequest> overlappingLeaves = leaveRequestRepository.findOverlappingLeaves(
            leaveRequest.getEmployeeId(),
            leaveRequest.getFromDate(),
            leaveRequest.getToDate()
        );
        
        // Filter out rejected leaves and the current request (if updating)
        List<LeaveRequest> conflictingLeaves = overlappingLeaves.stream()
            .filter(lr -> lr.getStatus() != LeaveStatus.REJECTED)
            .filter(lr -> !lr.getId().equals(leaveRequest.getId()))
            .toList();
        
        if (!conflictingLeaves.isEmpty()) {
            throw new IllegalArgumentException("Leave request overlaps with existing approved or pending leaves");
        }
    }
    
    private void applyPaidLeaveRule(LeaveRequest leaveRequest) {
        // Check if this is a paid leave type
        if (leaveRequest.getLeaveType() == LeaveType.CASUAL || 
            leaveRequest.getLeaveType() == LeaveType.SICK) {
            
            // Get existing paid leaves for this month
            int year = leaveRequest.getFromDate().getYear();
            int month = leaveRequest.getFromDate().getMonthValue();
            long paidLeavesUsed = leaveRequestDayRepository.countPaidLeavesByEmployeeAndMonth(
                    leaveRequest.getEmployeeId(), year, month);
            List<LeaveRequest> existingLeaves = leaveRequestRepository.findByEmployeeIdAndMonth(
                leaveRequest.getEmployeeId(), year, month);


            long paidLeaveCount = existingLeaves.stream()
                .filter(lr -> lr.getStatus() == LeaveStatus.APPROVED)
                .filter(lr -> lr.getLeaveType() == LeaveType.CASUAL || lr.getLeaveType() == LeaveType.SICK)
                .filter(lr -> !lr.getId().equals(leaveRequest.getId())) // Exclude current request if updating
                .count();
            
            if (paidLeaveCount >= 1 && !leaveRequest.getManualOverride()) {
                // Mark this leave as unpaid
                leaveRequest.setManualOverride(true);
                // You might want to notify the employee or admin
            }
        }
    }
    
 // In LeaveService.java - Add this method
    public Map<String, Object> getSandwichLeavesWithContext(String employeeId, LocalDate startDate, LocalDate endDate) {
        // Get all approved leaves in the date range
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedLeavesByEmployeeAndDateRange(
            employeeId, startDate, endDate);
        
        // Get sandwich leaves
        List<LeaveRequestDay> sandwichDays = leaveRequestDayRepository.findSandwichLeavesByEmployeeAndDateRange(
            employeeId, startDate, endDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("employeeId", employeeId);
        
        List<Map<String, Object>> sandwichLeavesList = new ArrayList<>();
        
        for (LeaveRequestDay sandwichDay : sandwichDays) {
            LocalDate sandwichDate = sandwichDay.getLeaveDate();
            
            // Find previous and next leaves
            Map<String, Object> previousLeave = findAdjacentLeave(approvedLeaves, sandwichDate, true);
            Map<String, Object> nextLeave = findAdjacentLeave(approvedLeaves, sandwichDate, false);
            
            if (previousLeave != null && nextLeave != null) {
                Map<String, Object> sandwichLeaveInfo = new HashMap<>();
                sandwichLeaveInfo.put("sandwichDate", sandwichDate);
                sandwichLeaveInfo.put("leaveCategory", sandwichDay.getLeaveCategory().name());
                sandwichLeaveInfo.put("previousLeave", previousLeave);
                sandwichLeaveInfo.put("nextLeave", nextLeave);
                
                // Generate message
                String message = String.format("%s is counted as a sandwich leave between your leave from %s to %s.",
                    sandwichDate,
                    previousLeave.get("toDate"),
                    nextLeave.get("fromDate"));
                sandwichLeaveInfo.put("message", message);
                
                sandwichLeavesList.add(sandwichLeaveInfo);
            }
        }
        
        response.put("sandwichLeaves", sandwichLeavesList);
        response.put("totalSandwichLeaves", sandwichLeavesList.size());
        
        return response;
    }

    private Map<String, Object> findAdjacentLeave(List<LeaveRequest> leaves, LocalDate targetDate, boolean findPrevious) {
        return leaves.stream()
            .filter(leave -> {
                if (findPrevious) {
                    return leave.getToDate().isBefore(targetDate) || leave.getToDate().equals(targetDate.minusDays(1));
                } else {
                    return leave.getFromDate().isAfter(targetDate) || leave.getFromDate().equals(targetDate.plusDays(1));
                }
            })
            .sorted((l1, l2) -> findPrevious ? 
                l2.getToDate().compareTo(l1.getToDate()) : // Latest end date for previous
                l1.getFromDate().compareTo(l2.getFromDate())) // Earliest start date for next
            .findFirst()
            .map(leave -> {
                Map<String, Object> leaveInfo = new HashMap<>();
                leaveInfo.put("id", leave.getId());
                leaveInfo.put("fromDate", leave.getFromDate());
                leaveInfo.put("toDate", leave.getToDate());
                leaveInfo.put("leaveType", leave.getLeaveType().name());
                leaveInfo.put("status", leave.getStatus().name());
                return leaveInfo;
            })
            .orElse(null);
    }
    private void detectAndMarkSandwichLeaves(LeaveRequest leaveRequest) {
        LocalDate startDate = leaveRequest.getFromDate();
        LocalDate endDate = leaveRequest.getToDate();
        
        // Check for leaves before this leave period
        List<LeaveRequest> previousLeaves = leaveRequestRepository.findByEmployeeIdAndStatus(
            leaveRequest.getEmployeeId(), LeaveStatus.APPROVED);
        
        for (LeaveRequest prevLeave : previousLeaves) {
            if (!prevLeave.getId().equals(leaveRequest.getId())) {
                LocalDate prevEnd = prevLeave.getToDate();
                
                // Check if there's a gap of 1-3 days between previous leave and current leave
                if (prevEnd.plusDays(1).isBefore(startDate) && 
                    prevEnd.plusDays(4).isAfter(startDate)) {
                    
                    // Check for holidays in the gap
                    List<Holiday> holidaysBetween = holidayService.getHolidaysBetween(
                        prevEnd.plusDays(1), startDate.minusDays(1));
                    
                    if (!holidaysBetween.isEmpty()) {
                        // This creates a sandwich situation - mark the holidays as sandwich leaves
                        markHolidaysAsSandwichLeaves(holidaysBetween, leaveRequest);
                    }
                }
            }
        }
        
        // Check for leaves after this leave period
        List<LeaveRequest> nextLeaves = leaveRequestRepository.findByEmployeeIdAndStatus(
            leaveRequest.getEmployeeId(), LeaveStatus.APPROVED);
        
        for (LeaveRequest nextLeave : nextLeaves) {
            if (!nextLeave.getId().equals(leaveRequest.getId())) {
                LocalDate nextStart = nextLeave.getFromDate();
                
                // Check if there's a gap of 1-3 days between current leave and next leave
                if (endDate.plusDays(1).isBefore(nextStart) && 
                    endDate.plusDays(4).isAfter(nextStart)) {
                    
                    // Check for holidays in the gap
                    List<Holiday> holidaysBetween = holidayService.getHolidaysBetween(
                        endDate.plusDays(1), nextStart.minusDays(1));
                    
                    if (!holidaysBetween.isEmpty()) {
                        // This creates a sandwich situation - mark the holidays as sandwich leaves
                        markHolidaysAsSandwichLeaves(holidaysBetween, leaveRequest);
                    }
                }
            }
        }
    }
    
    private void markHolidaysAsSandwichLeaves(List<Holiday> holidays, LeaveRequest leaveRequest) {
        for (Holiday holiday : holidays) {
            // Check if this holiday date already has a leave request day
            Optional<LeaveRequestDay> existingDayOpt = leaveRequest.getLeaveRequestDays().stream()
                .filter(day -> day.getLeaveDate().equals(holiday.getDate()))
                .findFirst();
            
            if (existingDayOpt.isPresent()) {
                // Update existing day
                LeaveRequestDay day = existingDayOpt.get();
                day.setSandwichFlag(true);
                leaveRequestDayRepository.save(day);
            } else {
                // Create new sandwich leave day
                LeaveRequestDay sandwichDay = new LeaveRequestDay();
                sandwichDay.setLeaveRequest(leaveRequest);
                sandwichDay.setLeaveDate(holiday.getDate());
                sandwichDay.setLeaveCategory(LeaveCategory.UNPAID); // Sandwich leaves are typically unpaid
                sandwichDay.setSandwichFlag(true);
                leaveRequestDayRepository.save(sandwichDay);
                
                // Add to the leave request's days collection
                leaveRequest.getLeaveRequestDays().add(sandwichDay);
            }
            
            // Update sandwich leave count in statistics
            updateSandwichLeaveStatistics(leaveRequest.getEmployeeId(), holiday.getDate());
        }
    }
    
    private void updateSandwichLeaveStatistics(String employeeId, LocalDate sandwichDate) {
        int year = sandwichDate.getYear();
        int month = sandwichDate.getMonthValue();
        
        EmployeeLeaveStatistics stats = employeeLeaveStatisticsRepository
            .findByEmployeeIdAndYearAndMonth(employeeId, year, month)
            .orElseGet(() -> {
                EmployeeLeaveStatistics newStats = new EmployeeLeaveStatistics();
                newStats.setEmployeeId(employeeId);
                newStats.setYear(year);
                newStats.setMonth(month);
                return newStats;
            });
        
        stats.setSandwichLeaveCount(stats.getSandwichLeaveCount() + 1);
        employeeLeaveStatisticsRepository.save(stats);
    }
    
 // In LeaveService class

 // Return per-day entries for a leave request
 public List<LeaveRequestDay> getLeaveRequestDays(Long leaveRequestId) {
     return leaveRequestDayRepository.findByLeaveRequestId(leaveRequestId);
 }

 // Optional convenience if you want to return DTOs/strings instead of entity objects
 public List<Map<String, Object>> getLeaveRequestDaysAsMap(Long leaveRequestId) {
     return getLeaveRequestDays(leaveRequestId).stream().map(d -> {
         Map<String, Object> m = new HashMap<>();
         m.put("id", d.getId());
         m.put("date", d.getLeaveDate()); // LocalDate -> Jackson will serialize as "YYYY-MM-DD"
         m.put("leavecategory", d.getLeaveCategory() != null ? d.getLeaveCategory().name() : "UNPAID");
         m.put("sandwichFlag", d.getSandwichFlag());
         m.put("otCreditUsed", d.getOtCreditUsed());
         return m;
     }).toList();
 }

    private void createLeaveRequestDays(LeaveRequest leaveRequest) {
        List<LeaveRequestDay> leaveDays = new ArrayList<>();
        LocalDate currentDate = leaveRequest.getFromDate();
        
        // Check how many paid leaves already used this month
        int year = leaveRequest.getFromDate().getYear();
        int month = leaveRequest.getFromDate().getMonthValue();
        
        long paidLeavesUsedThisMonth = leaveRequestDayRepository.countPaidLeavesByEmployeeAndMonth(
            leaveRequest.getEmployeeId(), year, month);
        
        boolean isPaidLeaveType = (leaveRequest.getLeaveType() == LeaveType.CASUAL || 
                                  leaveRequest.getLeaveType() == LeaveType.SICK);
        boolean allowPaid = isPaidLeaveType && !leaveRequest.getManualOverride();
        
        int paidDaysAllowed = allowPaid ? (int) (1 - paidLeavesUsedThisMonth) : 0;
        paidDaysAllowed = Math.max(0, paidDaysAllowed); // Ensure not negative
        
        while (!currentDate.isAfter(leaveRequest.getToDate())) {
            // Skip holidays (they'll be handled as sandwich leaves if applicable)
            if (!holidayService.isHoliday(currentDate)) {
                LeaveRequestDay day = new LeaveRequestDay();
                day.setLeaveRequest(leaveRequest);
                day.setLeaveDate(currentDate);
                
                // Determine leave category - only allow limited paid days
                if (paidDaysAllowed > 0) {
                    day.setLeaveCategory(LeaveCategory.PAID);
                    paidDaysAllowed--;
                } else {
                    day.setLeaveCategory(LeaveCategory.UNPAID);
                }
                
                day.setSandwichFlag(false); // Will be updated later if it's a sandwich leave
                leaveDays.add(day);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        leaveRequestDayRepository.saveAll(leaveDays);
        leaveRequest.setLeaveRequestDays(leaveDays);
    }
    
 // In your LeaveService class, find the updateEmployeeStatistics method
    private void updateEmployeeStatistics(LeaveRequest leaveRequest) {
        int year = leaveRequest.getFromDate().getYear();
        int month = leaveRequest.getFromDate().getMonthValue();
        
        EmployeeLeaveStatistics stats = employeeLeaveStatisticsRepository
            .findByEmployeeIdAndYearAndMonth(leaveRequest.getEmployeeId(), year, month)
            .orElseGet(() -> {
                EmployeeLeaveStatistics newStats = new EmployeeLeaveStatistics();
                newStats.setEmployeeId(leaveRequest.getEmployeeId());
                newStats.setYear(year);
                newStats.setMonth(month);
                return newStats;
            });
        
        // FIX: Handle null leaveDays
        int leaveDaysValue = leaveRequest.getLeaveDays() != null ? leaveRequest.getLeaveDays() : 0;
        long paidDays = leaveRequest.getLeaveRequestDays().stream()
                .filter(day -> day.getLeaveCategory() == LeaveCategory.PAID)
                .count();
            
        long unpaidDays = leaveRequest.getLeaveRequestDays().stream()
            .filter(day -> day.getLeaveCategory() == LeaveCategory.UNPAID)
            .count();
        
        // Update statistics based on the leave request
        if (leaveRequest.getStatus() == LeaveStatus.APPROVED) {
            stats.setTotalLeavesApproved(stats.getTotalLeavesApproved() + leaveDaysValue);
            
            if (leaveRequest.getLeaveDayType() == LeaveDayType.FULL_DAY) {
                stats.setFullDayLeavesApproved(stats.getFullDayLeavesApproved() + 1);
            } else {
                stats.setHalfDayLeavesApproved(stats.getHalfDayLeavesApproved() + 1);
            }
            stats.setPaidLeaveCount(stats.getPaidLeaveCount() + (int) paidDays);
            stats.setUnpaidLeaveCount(stats.getUnpaidLeaveCount() + (int) unpaidDays);
            
        } else if (leaveRequest.getStatus() == LeaveStatus.PENDING) {
            stats.setPendingLeaveCount(stats.getPendingLeaveCount() + 1);
        } else if (leaveRequest.getStatus() == LeaveStatus.REJECTED) {
            stats.setRejectedLeaveCount(stats.getRejectedLeaveCount() + 1);
        }
        
        stats.setLastUpdated(LocalDateTime.now());
        employeeLeaveStatisticsRepository.save(stats);
    }
 // In your LeaveService class, update the updateLeaveStatus method:
    @Transactional
    public LeaveRequest updateLeaveStatus(Long leaveRequestId, LeaveStatus status, String approvedBy) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));
        
        if (leaveRequest.getLeaveDays() == null) {
            // Calculate leave days if null
            int calculatedDays = calculateLeaveDays(leaveRequest.getFromDate(), leaveRequest.getToDate());
            leaveRequest.setLeaveDays(calculatedDays);
        }
        
        LeaveStatus oldStatus = leaveRequest.getStatus();
        leaveRequest.setStatus(status);
        leaveRequest.setActionDate(LocalDateTime.now());
        leaveRequest.setApprovedBy(approvedBy);
        
        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);
        
        // Update statistics if status changed
        if (!oldStatus.equals(status)) {
            updateEmployeeStatistics(updatedRequest);
        }
        
        return updatedRequest;
    }
    
    public List<LeaveRequest> getEmployeeLeaves(String employeeId, Integer year, Integer month) {
        if (year != null && month != null) {
            return leaveRequestRepository.findByEmployeeIdAndMonth(employeeId, year, month);
        } else {
            return leaveRequestRepository.findByEmployeeId(employeeId);
        }
    }
    public Optional<LeaveRequest> getLeaveRequestById(Long id) {
        return leaveRequestRepository.findById(id);
    }
 // In LeaveService.java
    public Optional<EmployeeLeaveStatistics> getEmployeeLeaveStats(String employeeId, int year, int month) {
        return employeeLeaveStatisticsRepository.findByEmployeeIdAndYearAndMonth(employeeId, year, month);
    }
    public List<LeaveRequestDay> getSandwichLeaves(String employeeId, LocalDate startDate, LocalDate endDate) {
        return leaveRequestDayRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate)
            .stream()
            .filter(LeaveRequestDay::getSandwichFlag)
            .toList();
    }
}