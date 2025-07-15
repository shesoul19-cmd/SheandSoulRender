package com.sheandsoul.v1update.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenstrualTrackingDto {

    private int cycleLength;
    private int periodLength;
    private LocalDate lastPeriodStartDate;
    private LocalDate lastPeriodEndDate;

}
