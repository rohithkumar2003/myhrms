package com.example.demo.util;

import com.example.demo.dto.AttendanceResponseDTO;
import com.example.demo.model.Attendance;

public class AttendanceConverter {
    
    public static AttendanceResponseDTO toDTO(Attendance attendance) {
        return new AttendanceResponseDTO(
            attendance.getId(),
            attendance.getEmployee().getEmployeeId(),
            attendance.getEmployee().getName(),
            attendance.getPunchInTime(),
            attendance.getPunchOutTime(),
            attendance.getHoursWorked(),
            attendance.getStatus(),
            attendance.getIdleTime(),
            attendance.getIsLateLogin(),
            attendance.getIsOtDay()
        );
    }
}