package com.perfectspring.topic04_webmvc.project;

/** Thrown by the service when an ID doesn't exist. GlobalErrorHandler turns it into a 404. */
public class HomeworkNotFoundException extends RuntimeException {

    public HomeworkNotFoundException(long id) {
        super("Homework #" + id + " not found");
    }
}
