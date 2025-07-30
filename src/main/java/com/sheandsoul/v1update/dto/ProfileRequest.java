package com.sheandsoul.v1update.dto;

import com.sheandsoul.v1update.entities.UserServiceType;
import com.sheandsoul.v1update.entities.Profile.UserType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfileRequest(
    @NotBlank String name,
    String nickname,
    @NotNull UserType userType,

    String preferredLanguage, // New field for preferred language
    // Fields for SELF_USE
    Integer age,
    Double weight,
    Double height,
    @NotNull UserServiceType preferredServiceType,
    // Field for PARTNER_USE
    String referredByCode
) {



}
