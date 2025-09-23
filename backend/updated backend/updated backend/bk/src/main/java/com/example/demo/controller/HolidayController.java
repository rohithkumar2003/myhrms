package com.example.demo.controller;

import com.example.demo.model.Holiday;
import com.example.demo.service.ExcelHelper;
import com.example.demo.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/holidays")
@CrossOrigin(origins="http://localhost:5173")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @PostMapping("/upload-excel")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<Holiday> holidays = ExcelHelper.convertExcelToList(file.getInputStream());
            System.out.println("📁 Received file: " + file.getOriginalFilename());
            if (holidays.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ No valid holidays found in the file.");
            }

            // Save to DB using service
            holidayService.replaceAllHolidays(holidays);
            return ResponseEntity.ok("✅ " + holidays.size() + " holidays successfully saved.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid data in Excel: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Failed to upload Excel");
        }
    }

    @PostMapping
    public ResponseEntity<Holiday> createHoliday(@RequestBody Holiday holiday) {
        Holiday saved = holidayService.addHoliday(holiday);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Holiday> update(@PathVariable Long id, @RequestBody Holiday updatedHoliday) {
        Holiday result = holidayService.updateHoliday(id, updatedHoliday);
        return ResponseEntity.ok(result);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping
    public List<Holiday> getAllHolidays() {
        return holidayService.getAllHolidays();
    }
}
