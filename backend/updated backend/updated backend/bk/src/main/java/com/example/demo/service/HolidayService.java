package com.example.demo.service;

import com.example.demo.model.Holiday;
import com.example.demo.repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    // EXISTING METHODS (UNCHANGED) - FOR HOLIDAY PAGE INTEGRATION
    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    public Holiday addHoliday(Holiday holiday) {
        return holidayRepository.save(holiday);
    }

    public void deleteHoliday(Long id) {
        holidayRepository.deleteById(id);
    }
    
    public Holiday updateHoliday(Long id, Holiday updatedHoliday) {
        return holidayRepository.findById(id).map(existing -> {
            existing.setName(updatedHoliday.getName());
            existing.setDate(updatedHoliday.getDate());
            existing.setDescription(updatedHoliday.getDescription());
            return holidayRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Holiday not found with id: " + id));
    }

    public void replaceAllHolidays(List<Holiday> newHolidays) {
        holidayRepository.deleteAll(); // Clear previous holidays
        holidayRepository.saveAll(newHolidays);
    }

    public List<Holiday> saveAll(List<Holiday> holidays) {
        return holidayRepository.saveAll(holidays);
    }

    // NEW METHODS ADDED FOR ATTENDANCE MODULE ONLY
    // These won't affect your existing holiday page functionality
    
    /**
     * Check if a specific date is a holiday
     * Used by AttendanceService for punch-in validation
     */
    public boolean isHoliday(LocalDate date) {
        return holidayRepository.existsByDate(date);
    }
    
    /**
     * Get holiday by specific date
     * Used by AttendanceService for punch-in validation
     */
    public Optional<Holiday> getHolidayByDate(LocalDate date) {
        return holidayRepository.findByDate(date);
    }
    
    /**
     * Get holidays within a date range
     * Could be useful for attendance reports
     */
    public List<Holiday> getHolidaysBetween(LocalDate startDate, LocalDate endDate) {
        return holidayRepository.findByDateBetween(startDate, endDate);
    }
}