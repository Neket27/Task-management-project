package app.controller.advice;


import app.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;

@ControllerAdvice
public class NotFoundExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public Response handleNotFoundException(NotFoundException e) {
        return new Response(HttpStatus.NOT_FOUND.value(), e.getMessage(), Instant.now());
    }
}