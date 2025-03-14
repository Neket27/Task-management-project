package app.controller.advice;

import java.time.Instant;

public record Response(
        Integer status,
        String error,
        Instant date) {
}