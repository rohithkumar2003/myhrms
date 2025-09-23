// PersonalDetailsRepository.java
package com.example.demo.repository;

import com.example.demo.model.PersonalDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalDetailsRepository extends JpaRepository<PersonalDetails, Long> {
    Optional<PersonalDetails> findByEmployeeEmployeeId(String employeeId);
    void deleteByEmployeeEmployeeId(String employeeId);
    boolean existsByEmployeeEmployeeId(String employeeId);
}

// ExperienceRepository.java
