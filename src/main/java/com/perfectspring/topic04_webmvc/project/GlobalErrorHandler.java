package com.perfectspring.topic04_webmvc.project;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * The COMPLAINTS DESK: catches exceptions thrown by ANY controller
 * and turns them into clean, consistent JSON error replies.
 */
@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(HomeworkNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(HomeworkNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));          // 404
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));        // 400
    }
}
