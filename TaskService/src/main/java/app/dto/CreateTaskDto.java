package app.dto;

public record CreateTaskDto(
        String title,
        String description,
        Long userId

) {
}