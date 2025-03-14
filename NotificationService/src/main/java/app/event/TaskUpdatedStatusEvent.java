package app.event;

public record TaskUpdatedStatusEvent(
        Long taskId,
        String status
) {}
