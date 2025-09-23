package com.example.demo.repository;

import com.example.demo.model.BankDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankDetailsRepository extends JpaRepository<BankDetails, Long> {
    
    // Change from Optional<BankDetails> to List<BankDetails>
    List<BankDetails> findByEmployeeEmployeeId(String employeeId);
    
    // Add a method to get the most recent bank details
    @Query("SELECT b FROM BankDetails b WHERE b.employee.employeeId = :employeeId ORDER BY b.id DESC")
    List<BankDetails> findByEmployeeEmployeeIdOrderByIdDesc(@Param("employeeId") String employeeId);
    
    // Helper method to get the latest bank details
    default Optional<BankDetails> findLatestByEmployeeEmployeeId(String employeeId) {
        List<BankDetails> bankDetails = findByEmployeeEmployeeIdOrderByIdDesc(employeeId);
        return bankDetails.isEmpty() ? Optional.empty() : Optional.of(bankDetails.get(0));
    }
    
    void deleteByEmployeeEmployeeId(String employeeId);
    boolean existsByEmployeeEmployeeId(String employeeId);
    
    // Add native query as backup
    @Query(value = "SELECT * FROM employee_bank_details WHERE employee_id = :employeeId", nativeQuery = true)
    List<BankDetails> findByEmployeeIdNative(@Param("employeeId") String employeeId);
}