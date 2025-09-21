package com.sheandsoul.v1update.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, max = 6) String otp,
    @NotBlank @Size(min = 8) String newPassword
) {}