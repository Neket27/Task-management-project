package app.dto;


import app.entity.Status;

public record UpdateTaskDto(
        String title,
        String description,
        Status status,
        Long userId
) {
}
