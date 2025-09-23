package com.example.demo.repository;

import com.example.demo.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    
    // Find holiday by specific date
    Optional<Holiday> findByDate(LocalDate date);
    
    // Check if a date is a holiday
    boolean existsByDate(LocalDate date);
    
    // Find holidays between date range
    List<Holiday> findByDateBetween(LocalDate startDate, LocalDate endDate);
    @Query("SELECT h FROM Holiday h WHERE h.date BETWEEN :startDate AND :endDate ORDER BY h.date")
    List<Holiday> findHolidaysInRange(@Param("startDate") LocalDate startDate, 
                                     @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(h) FROM Holiday h WHERE h.date BETWEEN :startDate AND :endDate")
    Long countHolidaysInRange(@Param("startDate") LocalDate startDate, 
                             @Param("endDate") LocalDate endDate);
    
    @Query("SELECT h.date FROM Holiday h WHERE h.date BETWEEN :startDate AND :endDate ORDER BY h.date")
    List<LocalDate> findHolidayDatesInRange(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    // Find holidays by year
    @Query("SELECT h FROM Holiday h WHERE YEAR(h.date) = :year ORDER BY h.date")
    List<Holiday> findByYear(@Param("year") int year);
    
    // Find holidays by month and year
    @Query("SELECT h FROM Holiday h WHERE MONTH(h.date) = :month AND YEAR(h.date) = :year ORDER BY h.date")
    List<Holiday> findByMonthAndYear(@Param("month") int month, @Param("year") int year);
    
    // Find upcoming holidays
    @Query("SELECT h FROM Holiday h WHERE h.date >= :today ORDER BY h.date")
    List<Holiday> findUpcomingHolidays(@Param("today") LocalDate today);
}