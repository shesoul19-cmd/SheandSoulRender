package com.sheandsoul.v1update.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CyclePredictionDto {

    private LocalDate nextPeriodStartDate;
    private LocalDate nextPeriodEndDate;

    private LocalDate nextOvulationDate;
    private LocalDate nextOvulationEndDate;

    private LocalDate nextFertileWindowStartDate;
    private LocalDate nextFertileWindowEndDate;

    private LocalDate nextFollicularStartDate;
    private LocalDate nextFollicularEndDate;

    private LocalDate nextLutealStartDate;
    private LocalDate nextLutealEndDate;

}
