package com.perfectspring.topic02_core.example.ex1_componentscan;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * EXAMPLE 1 — @Component + component scanning + constructor autowiring
 *
 * Story: staff wear BADGES (@Component/@Service/@Repository).
 * HR walks the corridors (@ComponentScan) and registers everyone wearing one.
 * Then HR reads each constructor ("what does this room need?") and delivers helpers.
 *
 * Notice: there is NOT A SINGLE `new` for our classes in this file's main method.
 */
public class ComponentScanExample {

    @Repository                    // badge: "I'm the records officer"
    static class StudentRecords {
        String findName(int id) {
            return id == 1 ? "Ada Lovelace" : "Unknown student";
        }
    }

    @Component                     // badge: "I'm a general helper"
    static class Greeter {
        String greet(String name) {
            return "Welcome to school, " + name + "!";
        }
    }

    @Service                       // badge: "I'm a teacher (business logic)"
    static class ReceptionService {
        private final StudentRecords records;
        private final Greeter greeter;

        // Spring reads this constructor and passes in the two beans automatically.
        ReceptionService(StudentRecords records, Greeter greeter) {
            this.records = records;
            this.greeter = greeter;
        }

        void welcome(int studentId) {
            System.out.println(greeter.greet(records.findName(studentId)));
        }
    }

    /** Tiny config: "HR, scan THIS package (and below) for badges." */
    @Configuration
    @ComponentScan
    static class ScanConfig {
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(ScanConfig.class)) {
            ReceptionService reception = context.getBean(ReceptionService.class);
            reception.welcome(1);
        }
    }
}
