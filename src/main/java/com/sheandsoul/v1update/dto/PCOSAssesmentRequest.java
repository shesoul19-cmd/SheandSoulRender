package com.sheandsoul.v1update.dto;

import java.util.Map;

import com.sheandsoul.v1update.entities.PCOSSymptoms;
import com.sheandsoul.v1update.entities.SymptomSeverity;

import jakarta.validation.constraints.NotEmpty;

public record PCOSAssesmentRequest(
    @NotEmpty
    Map<PCOSSymptoms, SymptomSeverity> symptoms
) {

}
