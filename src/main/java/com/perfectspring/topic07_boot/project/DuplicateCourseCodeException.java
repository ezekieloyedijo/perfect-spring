package com.perfectspring.topic07_boot.project;

/** Becomes a 409 Conflict in ApiErrorHandler: the course code is already taken. */
public class DuplicateCourseCodeException extends RuntimeException {

    public DuplicateCourseCodeException(String code) {
        super("A course with code " + code + " already exists");
    }
}
