package app.dto;

import app.entity.Status;

public record TaskDto(
        Long id,
        String title,
        String description,
        Status status,
        Long userId
) {}
