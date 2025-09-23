package com.example.demo.service;

import com.example.demo.model.Overtime;
import com.example.demo.model.Employee;
import com.example.demo.model.Overtime.OTType;
import com.example.demo.model.Overtime.OTStatus;
import com.example.demo.repository.OvertimeRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.dto.OvertimeResponseDTO;
import com.example.demo.util.OvertimeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OvertimeService {

    @Autowired
    private OvertimeRepository overtimeRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Request OT - Updated to return DTO
    public OvertimeResponseDTO requestOvertime(String employeeId, LocalDate date, OTType type) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Overtime overtime = new Overtime();
        overtime.setEmployee(employee);
        overtime.setDate(date);
        overtime.setType(type);
        overtime.setStatus(OTStatus.PENDING);
        overtime.setIsUsedAsLeave(false);
        overtime.setIsPaidOut(false);

        Overtime savedOvertime = overtimeRepository.save(overtime);
        return OvertimeConverter.toDTO(savedOvertime);
    }

    // Approve OT - Updated to return DTO
    public OvertimeResponseDTO approveOvertime(Long otId, OTType type) {
        Overtime overtime = overtimeRepository.findById(otId)
                .orElseThrow(() -> new ResourceNotFoundException("OT request not found"));

        overtime.setStatus(OTStatus.APPROVED);
        overtime.setType(type);
        
        Overtime savedOvertime = overtimeRepository.save(overtime);
        return OvertimeConverter.toDTO(savedOvertime);
    }

    // Reject OT - Updated to return DTO
    public OvertimeResponseDTO rejectOvertime(Long otId) {
        Overtime overtime = overtimeRepository.findById(otId)
                .orElseThrow(() -> new ResourceNotFoundException("OT request not found"));

        overtime.setStatus(OTStatus.REJECTED);
        Overtime savedOvertime = overtimeRepository.save(overtime);
        return OvertimeConverter.toDTO(savedOvertime);
    }

    // Get all OT requests for employee - Updated to return DTO
    public List<OvertimeResponseDTO> getEmployeeOvertime(String employeeId) {
        List<Overtime> overtimeList = overtimeRepository.findByEmployeeEmployeeId(employeeId);
        return OvertimeConverter.toDTOList(overtimeList);
    }

    // Get pending OT requests - Updated to return DTO
    public List<OvertimeResponseDTO> getPendingOvertime() {
        List<Overtime> pendingOvertime = overtimeRepository.findByStatus(OTStatus.PENDING);
        return OvertimeConverter.toDTOList(pendingOvertime);
    }

    // Update employee OT stats after punch-out - UNCHANGED (void method)
    @Transactional
    public void updateOTStatsAfterPunchOut(String employeeId, LocalDate date, double hoursWorked) {
        try {
            Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
            
            Optional<Overtime> overtimeOpt = overtimeRepository.findByEmployeeEmployeeIdAndDate(employeeId, date);
            
            if (overtimeOpt.isPresent()) {
                Overtime overtime = overtimeOpt.get();
                boolean meetsHoursRequirement = hoursWorked >= 4;
                boolean isApproved = overtime.getStatus() == OTStatus.APPROVED;
                
                if (meetsHoursRequirement && isApproved) {
                    if (overtime.getType() == OTType.PENDING_OT) {
                        int currentPending = employee.getOtPendingDays() != null ? employee.getOtPendingDays() : 0;
                        employee.setOtPendingDays(currentPending + 1);
                        
                        employeeRepository.save(employee);
                        
                        overtime.setIsPaidOut(true);
                        overtimeRepository.save(overtime);
                    }
                    else if (overtime.getType() == OTType.INCENTIVE_OT) {
                        int currentIncentive = employee.getOtIncentiveDays() != null ? employee.getOtIncentiveDays() : 0;
                        employee.setOtIncentiveDays(currentIncentive + 1);
                        
                        employeeRepository.save(employee);
                        
                        overtime.setIsPaidOut(true);
                        overtimeRepository.save(overtime);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error updating OT stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Admin: Allocate OT to employee by ID or name - Updated to return DTO
    @Transactional
    public OvertimeResponseDTO allocateOvertimeAdmin(String employeeId, String employeeName, LocalDate date, 
                                      OTType type, OTStatus status, String reason) {
     
        Employee employee = findEmployeeByIdOrName(employeeId, employeeName);
        
        if (employee == null) {
            throw new ResourceNotFoundException("Employee not found with provided ID or name");
        }

        Optional<Overtime> existingOT = overtimeRepository.findByEmployeeEmployeeIdAndDate(employee.getEmployeeId(), date);
        if (existingOT.isPresent()) {
            throw new RuntimeException("OT already allocated for employee " + employee.getEmployeeId() + " on date " + date);
        }

        Overtime overtime = new Overtime();
        overtime.setEmployee(employee);
        overtime.setDate(date);
        overtime.setType(type);
        overtime.setStatus(status);
        overtime.setIsUsedAsLeave(false);
        overtime.setIsPaidOut(false);

        Overtime savedOvertime = overtimeRepository.save(overtime);
        return OvertimeConverter.toDTO(savedOvertime);
    }

    // Admin: Bulk allocate OT to multiple employees - Updated to return DTO
    @Transactional
    public List<OvertimeResponseDTO> bulkAllocateOvertime(List<String> employeeIds, LocalDate date, 
                                           OTType type, OTStatus status, String reason) {
     
        return employeeIds.stream()
                .map(employeeId -> {
                    try {
                        return allocateOvertimeAdmin(employeeId, null, date, type, status, reason);
                    } catch (Exception e) {
                        System.out.println("Failed to allocate OT for employee " + employeeId + ": " + e.getMessage());
                        return null;
                    }
                })
                .filter(ot -> ot != null)
                .collect(Collectors.toList());
    }

    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByCurrentDepartment(department);
    }

    // Helper method to find employee by ID or name - UNCHANGED
    private Employee findEmployeeByIdOrName(String employeeId, String employeeName) {
        if (employeeId != null && !employeeId.trim().isEmpty()) {
            return employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        } else if (employeeName != null && !employeeName.trim().isEmpty()) {
            List<Employee> employees = employeeRepository.findByNameContaining(employeeName);
            if (employees.isEmpty()) {
                throw new ResourceNotFoundException("Employee not found with name: " + employeeName);
            }
            if (employees.size() > 1) {
                throw new RuntimeException("Multiple employees found with name: " + employeeName + ". Please use employee ID.");
            }
            return employees.get(0);
        }
        throw new RuntimeException("Either employeeId or employeeName must be provided");
    }

    // Admin: Get all OT allocations with filters - Updated to return DTO
    public List<OvertimeResponseDTO> getAllocations(String employeeId, LocalDate startDate, LocalDate endDate, OTStatus status) {
        List<Overtime> allocations;
        
        if (employeeId != null && startDate != null && endDate != null && status != null) {
            allocations = overtimeRepository.findByEmployeeEmployeeIdAndDateBetweenAndStatus(employeeId, startDate, endDate, status);
        } else if (employeeId != null && startDate != null && endDate != null) {
            allocations = overtimeRepository.findByEmployeeEmployeeIdAndDateBetween(employeeId, startDate, endDate);
        } else if (employeeId != null && status != null) {
            allocations = overtimeRepository.findByEmployeeEmployeeIdAndStatus(employeeId, status);
        } else if (startDate != null && endDate != null) {
            allocations = overtimeRepository.findByDateBetween(startDate, endDate);
        } else if (status != null) {
            allocations = overtimeRepository.findByStatus(status);
        } else if (employeeId != null) {
            allocations = overtimeRepository.findByEmployeeEmployeeId(employeeId);
        } else {
            allocations = overtimeRepository.findAll();
        }
        
        return OvertimeConverter.toDTOList(allocations);
    }

    // Admin: Update OT allocation - Updated to return DTO
    @Transactional
    public OvertimeResponseDTO updateAllocation(Long allocationId, OTType type, OTStatus status, String reason) {
        Overtime overtime = overtimeRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("OT allocation not found"));
        
        if (type != null) {
            overtime.setType(type);
        }
        if (status != null) {
            overtime.setStatus(status);
        }
        
        Overtime savedOvertime = overtimeRepository.save(overtime);
        return OvertimeConverter.toDTO(savedOvertime);
    }

    public List<String> getAllDepartments() {
        return employeeRepository.findAllCurrentDepartments();
    }

    public List<Employee> searchEmployeesByDepartment(String query, String department) {
        if (department != null && !department.isEmpty()) {
            return employeeRepository.searchEmployeesByDepartment(query, department);
        } else {
            return employeeRepository.searchEmployees(query);
        }
    }

    // Admin: Allocate OT to department - Updated to return DTO
    @Transactional
    public List<OvertimeResponseDTO> allocateOvertimeToDepartment(String department, LocalDate date, 
                                                   OTType type, OTStatus status, String reason) {
     
        List<Employee> departmentEmployees = employeeRepository.findByCurrentDepartment(department);
        List<String> employeeIds = departmentEmployees.stream()
                .map(Employee::getEmployeeId)
                .collect(Collectors.toList());
        
        return bulkAllocateOvertime(employeeIds, date, type, status, reason);
    }

    // Admin: Delete OT allocation - UNCHANGED (void method)
    @Transactional
    public void deleteAllocation(Long allocationId) {
        if (!overtimeRepository.existsById(allocationId)) {
            throw new ResourceNotFoundException("OT allocation not found");
        }
        overtimeRepository.deleteById(allocationId);
    }
}