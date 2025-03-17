package app.controller.advice;

import app.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice(basePackages = "app.controller")
@Slf4j
public class NotFoundExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public Response handleNotFoundException(NotFoundException e) {
        log.error("NotFoundException caught: {}", e.getMessage());
        return new Response(HttpStatus.NOT_FOUND.value(), e.getMessage(), Instant.now());
    }
}