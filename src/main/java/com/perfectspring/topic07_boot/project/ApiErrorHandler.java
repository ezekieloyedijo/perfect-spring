package com.perfectspring.topic07_boot.project;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/** The complaints desk (Topic 4), now also handling VALIDATION errors. */
@RestControllerAdvice
public class ApiErrorHandler {

    /** Thrown by Spring when @Valid finds broken rules -> 400 with one message per field. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("error", "Validation failed", "fields", fieldErrors));
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(CourseNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    /** Both "course is full" and "code already taken" conflict with the current data -> 409. */
    @ExceptionHandler({CourseFullException.class, DuplicateCourseCodeException.class})
    public ResponseEntity<Map<String, String>> conflict(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }
}
