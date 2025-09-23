package com.example.demo.service;

import com.example.demo.model.Holiday;
import org.apache.poi.ss.usermodel.*;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelHelper {

    public static List<Holiday> convertExcelToList(InputStream is) {
        List<Holiday> holidays = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            boolean isFirstRow = true;

            while (rows.hasNext()) {
                Row row = rows.next();

                // Skip header
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                // Read and validate cells
                Cell nameCell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell dateCell = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell descCell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                if (nameCell == null || nameCell.getCellType() == CellType.BLANK) {
                    System.out.println("Skipping row " + row.getRowNum() + ": Holiday name is blank.");
                    continue;
                }

                if (dateCell == null) {
                    System.out.println("Skipping row " + row.getRowNum() + ": Date cell is null.");
                    continue;
                }

                try {
                	String name = getCellValueAsString(nameCell);
                    LocalDate date;

                    if (dateCell.getCellType() == CellType.STRING) {
                        String dateStr = dateCell.getStringCellValue().trim();
                        date = LocalDate.parse(dateStr, formatter);
                    } else if (dateCell.getCellType() == CellType.NUMERIC) {
                        if (DateUtil.isCellDateFormatted(dateCell)) {
                            date = dateCell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        } else {
                            System.out.println("Skipping row " + row.getRowNum() + ": Numeric but not a date.");
                            continue;
                        }
                    } else {
                        System.out.println("Skipping row " + row.getRowNum() + ": Unsupported cell type for date.");
                        continue;
                    }


                    String description = (descCell != null) ? getCellValueAsString(descCell) : "";

                    Holiday holiday = new Holiday();
                    holiday.setName(name);
                    holiday.setDate(date);
                    holiday.setDescription(description);

                    holidays.add(holiday);

                    // Debug output
                    System.out.println("✅ Parsed Row: " + name + " | " + date + " | " + description);

                } catch (Exception e) {
                    System.out.println("❌ Error parsing row " + row.getRowNum() + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("🚫 Failed to parse Excel: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("✅ Total valid holidays parsed: " + holidays.size());
        return holidays;
    }
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue()).trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()).trim();
            case FORMULA:
                return cell.getCellFormula().trim();
            case BLANK:
                return "";
            default:
                return cell.toString().trim();
        }
    }

}
