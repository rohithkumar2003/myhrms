package com.example.demo.service;

import com.example.demo.model.DepartmentSettings;
import com.example.demo.repository.DepartmentSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DepartmentSettingsService {

    @Autowired
    private DepartmentSettingsRepository departmentSettingsRepository;

    public List<DepartmentSettings> getAllDepartmentSettings() {
        return departmentSettingsRepository.findAll();
    }

    public Optional<DepartmentSettings> getDepartmentSettingsById(Long id) {
        return departmentSettingsRepository.findById(id);
    }

    public Optional<DepartmentSettings> getDepartmentSettingsByNameAndType(String departmentName, String empType) {
        return departmentSettingsRepository.findByDepartmentNameAndEmpTypeWithFallback(departmentName, empType);
    }

    public Optional<DepartmentSettings> getDepartmentSettingsByName(String departmentName) {
        return departmentSettingsRepository.findByDepartmentName(departmentName);
    }

    public DepartmentSettings createDepartmentSettings(DepartmentSettings settings) {
        return departmentSettingsRepository.save(settings);
    }

    public DepartmentSettings updateDepartmentSettings(Long id, DepartmentSettings settingsDetails) {
        return departmentSettingsRepository.findById(id).map(settings -> {
            settings.setDepartmentName(settingsDetails.getDepartmentName());
            settings.setEmpType(settingsDetails.getEmpType());
            settings.setPunchInStart(settingsDetails.getPunchInStart());
            settings.setOfficeStart(settingsDetails.getOfficeStart());
            settings.setOfficeEnd(settingsDetails.getOfficeEnd());
            settings.setLateLoginThreshold(settingsDetails.getLateLoginThreshold());
            settings.setHalfDayThreshold(settingsDetails.getHalfDayThreshold());
            settings.setFullDayThreshold(settingsDetails.getFullDayThreshold());
            settings.setMorningHalfLogin(settingsDetails.getMorningHalfLogin());
            settings.setMorningHalfLogout(settingsDetails.getMorningHalfLogout());
            settings.setAfternoonHalfLogin(settingsDetails.getAfternoonHalfLogin());
            settings.setAfternoonHalfLogout(settingsDetails.getAfternoonHalfLogout());
            return departmentSettingsRepository.save(settings);
        }).orElseThrow(() -> new RuntimeException("Department settings not found with id: " + id));
    }

    public void deleteDepartmentSettings(Long id) {
        departmentSettingsRepository.deleteById(id);
    }

    public boolean departmentSettingsExists(String departmentName) {
        return departmentSettingsRepository.existsByDepartmentName(departmentName);
    }

    public Optional<DepartmentSettings> getSettingsForEmployee(String departmentName, String empType) {
        // First try to find specific emp_type settings
        Optional<DepartmentSettings> specificSettings = departmentSettingsRepository.findByDepartmentNameAndEmpType(departmentName, empType);
        
        if (specificSettings.isPresent()) {
            return specificSettings;
        }
        
        // Fallback to DEFAULT emp_type
        return departmentSettingsRepository.findDefaultByDepartmentName(departmentName);
    }
}