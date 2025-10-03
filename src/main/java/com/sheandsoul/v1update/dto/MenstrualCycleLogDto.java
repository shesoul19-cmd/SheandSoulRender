package com.sheandsoul.v1update.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class MenstrualCycleLogDto {
    private Long id;
    private int periodLength;
    private int cycleLength;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private LocalDate logDate;

    // A static factory method can be useful for conversion
    public static MenstrualCycleLogDto fromEntity(com.sheandsoul.v1update.entities.MenstrualCycleLog entity) {
        MenstrualCycleLogDto dto = new MenstrualCycleLogDto();
        dto.setId(entity.getId());
        dto.setPeriodLength(entity.getPeriodLength());
        dto.setCycleLength(entity.getCycleLength());
        dto.setPeriodStartDate(entity.getPeriodStartDate());
        dto.setPeriodEndDate(entity.getPeriodEndDate());
        dto.setLogDate(entity.getLogDate());
        return dto;
    }
}