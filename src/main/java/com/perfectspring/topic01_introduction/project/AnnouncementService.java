package com.perfectspring.topic01_introduction.project;

import java.util.List;

/**
 * The BUSINESS LOGIC: "send an announcement to every student".
 *
 * Notice: no `new EmailSender()` anywhere in here!
 * This class only ASKS for "some MessageSender" in its constructor.
 * Spring decides which one to hand in (see AppConfig).
 */
public class AnnouncementService {

    private final MessageSender messageSender;

    public AnnouncementService(MessageSender messageSender) {   // constructor injection
        this.messageSender = messageSender;
    }

    public void announce(List<String> students, String message) {
        System.out.println("Sending announcement to " + students.size() + " students...");
        for (String student : students) {
            messageSender.send(student, message);
        }
        System.out.println("Done.");
    }
}
