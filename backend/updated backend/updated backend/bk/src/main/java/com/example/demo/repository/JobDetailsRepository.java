
// JobDetailsRepository.java
package com.example.demo.repository;

import com.example.demo.model.JobDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobDetailsRepository extends JpaRepository<JobDetails, Long> {
    Optional<JobDetails> findByEmployeeEmployeeId(String employeeId);
    void deleteByEmployeeEmployeeId(String employeeId);
    boolean existsByEmployeeEmployeeId(String employeeId);
}