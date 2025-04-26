package ru.practicum.shareit.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> dealWithNotFoundException(NotFoundException e) {
        Map<String, String> response = new HashMap<>();
        log.error("error", e.getMessage());
        response.put("error", e.getMessage());
        return response;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> dealWithValidationException(EmailExistsException e) {
        Map<String, String> response = new HashMap<>();
        log.error("error", e.getMessage());
        response.put("error:", e.getMessage());
        return response;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> dealWithAnyException(Throwable e) {
        Map<String, String> response = new HashMap<>();
        log.error("some error", e.getMessage());
        response.put("some error:", e.getMessage());
        return response;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> dealWithEmptyInformatonException(EmptyInformationException e) {
        Map<String, String> response = new HashMap<>();
        log.error("error", e.getMessage());
        response.put("error", e.getMessage());
        return response;
    }

}