package com.perfectspring.topic01_introduction.project;

/** Another real worker that fits the MessageSender job description. */
public class SmsSender implements MessageSender {

    @Override
    public void send(String to, String message) {
        System.out.println("[SMS] to " + to + "'s phone : " + message);
    }
}
