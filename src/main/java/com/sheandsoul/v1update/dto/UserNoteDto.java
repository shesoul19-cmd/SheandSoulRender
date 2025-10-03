package com.sheandsoul.v1update.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserNoteDto {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}