package com.perfectspring.topic06_testing.project;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** @ResponseStatus: a short way to say "when this escapes a controller, reply with 404". */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(Long studentId) {
        super("Student #" + studentId + " not found");
    }
}
