package com.example.demo.service;

import com.example.demo.model.Holiday;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ExcelService {

    @Autowired
    private HolidayService holidayService;

    public void saveExcel(MultipartFile file) {
        try {
            List<Holiday> holidays = ExcelHelper.convertExcelToList(file.getInputStream());
            System.out.println("Parsed holidays: " + holidays.size());
            for (Holiday h : holidays) {
                System.out.println(h); // for debugging
            }
            holidayService.saveAll(holidays);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Excel processing failed: " + e.getMessage());
        }
    }
}
