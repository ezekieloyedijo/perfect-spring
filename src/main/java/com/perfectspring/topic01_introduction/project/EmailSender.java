package com.perfectspring.topic01_introduction.project;

/** One real worker that fits the MessageSender job description. */
public class EmailSender implements MessageSender {

    @Override
    public void send(String to, String message) {
        System.out.println("[EMAIL] to " + to + "@school.edu : " + message);
    }
}
