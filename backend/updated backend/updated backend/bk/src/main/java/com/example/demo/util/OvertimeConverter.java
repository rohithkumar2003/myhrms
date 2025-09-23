package com.example.demo.util;

import com.example.demo.dto.OvertimeResponseDTO;
import com.example.demo.model.Overtime;
import java.util.List;
import java.util.stream.Collectors;

public class OvertimeConverter {
    
    public static OvertimeResponseDTO toDTO(Overtime overtime) {
        return new OvertimeResponseDTO(overtime);
    }
    
    public static List<OvertimeResponseDTO> toDTOList(List<Overtime> overtimes) {
        return overtimes.stream()
                .map(OvertimeResponseDTO::new)
                .collect(Collectors.toList());
    }
}