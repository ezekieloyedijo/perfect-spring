package com.perfectspring.topic07_boot.project;

/** Becomes a 409 Conflict in ApiErrorHandler ("the request conflicts with the current state"). */
public class CourseFullException extends RuntimeException {

    public CourseFullException(String message) {
        super(message);
    }
}
