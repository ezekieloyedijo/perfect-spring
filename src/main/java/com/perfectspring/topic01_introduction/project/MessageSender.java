package com.perfectspring.topic01_introduction.project;

/**
 * The "job description" (interface).
 * Anyone who can send(...) a message qualifies for the job.
 * AnnouncementService only ever talks to this shape, never to a specific sender.
 */
public interface MessageSender {

    void send(String to, String message);
}
