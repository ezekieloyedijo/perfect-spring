package com.perfectspring.topic02_core.project;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** Candidate #1. @Primary = "use me when nobody asks for a specific Notifier". */
@Component
@Primary
public class ConsoleNotifier implements Notifier {

    @Override
    public void notify(String student, String message) {
        System.out.println("   [screen] " + student + ": " + message);
    }
}
