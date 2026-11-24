package com.perfectspring.topic07_boot.project;

/** Becomes a 404 in ApiErrorHandler. */
public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(Long id) {
        super("Course #" + id + " not found");
    }
}
