package com.sheandsoul.v1update.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO for authentication, aligning with Android client expectations.
 */
@Data
@AllArgsConstructor
public class AuthResponseDto {
    private String message;

    @JsonProperty("user_id")
    private Long userId;

    private String email;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType = "bearer";
    /**
     * Convenience constructor to set tokenType to default "bearer".
     */
    public AuthResponseDto(String message, Long userId, String email, String accessToken) {
        this.message = message;
        this.userId = userId;
        this.email = email;
        this.accessToken = accessToken;
        this.tokenType = "bearer";
    }
}
