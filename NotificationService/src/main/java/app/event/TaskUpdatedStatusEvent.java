package app.event;

import app.entity.Status;

public record TaskUpdatedStatusEvent(
        Long taskId,
        Status status
){}
