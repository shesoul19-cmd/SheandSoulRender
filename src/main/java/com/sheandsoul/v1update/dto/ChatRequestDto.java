package com.sheandsoul.v1update.dto;

import com.sheandsoul.v1update.entities.HealthServiceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequestDto(
    @NotBlank String message,
    @NotNull HealthServiceType healthServiceType,
    String language
) {

}
