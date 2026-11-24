package com.perfectspring.topic02_core.example.ex6_profiles;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * EXAMPLE 6 — Profiles: different beans for different situations
 *
 * Story: the same school has a TERM timetable and a HOLIDAY timetable.
 * Which one is pinned up depends on the active profile.
 *
 * Try: change "term" to "holiday" in main() and run again.
 */
public class ProfileExample {

    interface BellSchedule {
        String firstBell();
    }

    @Component
    @Profile("term")
    static class TermBells implements BellSchedule {
        public String firstBell() { return "07:45 - assembly, then lessons"; }
    }

    @Component
    @Profile("holiday")
    static class HolidayBells implements BellSchedule {
        public String firstBell() { return "10:00 - optional summer coding club"; }
    }

    @Configuration
    @ComponentScan
    static class Config {
    }

    public static void main(String[] args) {
        String activeProfile = "term";       // 👈 try "holiday"

        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles(activeProfile); // choose the situation FIRST
            context.register(Config.class);                            // then give the plan
            context.refresh();                                         // then start the container

            BellSchedule bells = context.getBean(BellSchedule.class);
            System.out.println("Active profile: " + activeProfile);
            System.out.println("First bell:     " + bells.firstBell());
        }
    }
}
