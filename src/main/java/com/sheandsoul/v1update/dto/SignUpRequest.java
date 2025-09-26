package com.sheandsoul.v1update.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Optional;

public record SignUpRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    Optional<String> deviceToken
) {
} 

