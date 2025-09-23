package com.example.demo.controller;

import com.example.demo.model.DepartmentSettings;
import com.example.demo.service.DepartmentSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/department-settings")
@CrossOrigin(origins = "http://localhost:5173")
public class DepartmentSettingsController {

    @Autowired
    private DepartmentSettingsService departmentSettingsService;

    @GetMapping
    public List<DepartmentSettings> getAllDepartmentSettings() {
        return departmentSettingsService.getAllDepartmentSettings();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentSettings> getDepartmentSettingsById(@PathVariable Long id) {
        Optional<DepartmentSettings> settings = departmentSettingsService.getDepartmentSettingsById(id);
        return settings.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/department/{departmentName}/emp-type/{empType}")
    public ResponseEntity<DepartmentSettings> getDepartmentSettingsByNameAndType(
            @PathVariable String departmentName, @PathVariable String empType) {
        Optional<DepartmentSettings> settings = departmentSettingsService.getSettingsForEmployee(departmentName, empType);
        return settings.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/department/{departmentName}")
    public ResponseEntity<DepartmentSettings> getDepartmentSettingsByName(@PathVariable String departmentName) {
        Optional<DepartmentSettings> settings = departmentSettingsService.getDepartmentSettingsByName(departmentName);
        return settings.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public DepartmentSettings createDepartmentSettings(@RequestBody DepartmentSettings settings) {
        return departmentSettingsService.createDepartmentSettings(settings);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentSettings> updateDepartmentSettings(@PathVariable Long id, @RequestBody DepartmentSettings settingsDetails) {
        try {
            DepartmentSettings updatedSettings = departmentSettingsService.updateDepartmentSettings(id, settingsDetails);
            return ResponseEntity.ok(updatedSettings);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartmentSettings(@PathVariable Long id) {
        departmentSettingsService.deleteDepartmentSettings(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/{departmentName}")
    public ResponseEntity<Boolean> checkDepartmentSettingsExists(@PathVariable String departmentName) {
        boolean exists = departmentSettingsService.departmentSettingsExists(departmentName);
        return ResponseEntity.ok(exists);
    }
}