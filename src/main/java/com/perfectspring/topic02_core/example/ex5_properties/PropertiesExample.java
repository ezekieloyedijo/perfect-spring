package com.perfectspring.topic02_core.example.ex5_properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * EXAMPLE 5 — Reading settings from a .properties file
 *
 * Story: the school RULE BOOK on the notice board.
 * File: src/main/resources/topic02/school.properties
 *
 * Try: change the values in that file and run again. No Java edits needed!
 */
public class PropertiesExample {

    @Component
    static class SchoolInfo {
        private final String name;
        private final int maxStudentsPerClass;
        private final String motto;

        SchoolInfo(@Value("${school.name}") String name,
                   @Value("${school.max-students-per-class}") int maxStudentsPerClass,
                   @Value("${school.motto:Learn and Grow}") String motto) {   // default if missing
            this.name = name;
            this.maxStudentsPerClass = maxStudentsPerClass;
            this.motto = motto;
        }

        void print() {
            System.out.println("School:          " + name);
            System.out.println("Max per class:   " + maxStudentsPerClass);
            System.out.println("Motto:           " + motto + "   <- not in the file, so the default was used");
        }
    }

    @Configuration
    @ComponentScan
    @PropertySource("classpath:topic02/school.properties")   // pin the rule book on the board
    static class Config {
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            context.getBean(SchoolInfo.class).print();
        }
    }
}
