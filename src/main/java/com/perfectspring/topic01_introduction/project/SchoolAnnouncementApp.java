package com.perfectspring.topic01_introduction.project;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

/**
 * PROJECT 1 — School Announcement System
 *
 * Run me. Then open AppConfig and switch Email -> SMS.
 * Run me again: different behaviour, and AnnouncementService was never touched.
 */
public class SchoolAnnouncementApp {

    public static void main(String[] args) {
        // Open the HR office with our hiring plan.
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {

            // Ask for the fully-wired service.
            AnnouncementService service = context.getBean(AnnouncementService.class);

            service.announce(
                    List.of("ada", "tunde", "chioma"),
                    "Mid-term break starts on Friday. Enjoy!"
            );
        }
    }
}
