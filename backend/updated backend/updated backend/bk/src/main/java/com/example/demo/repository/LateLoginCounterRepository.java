package com.example.demo.repository;

import com.example.demo.model.LateLoginCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LateLoginCounterRepository extends JpaRepository<LateLoginCounter, Long> {
    Optional<LateLoginCounter> findByEmployeeEmployeeIdAndMonthAndYear(String employeeId, Integer month, Integer year);
}