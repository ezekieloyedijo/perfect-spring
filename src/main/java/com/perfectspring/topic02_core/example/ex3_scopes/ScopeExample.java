package com.perfectspring.topic02_core.example.ex3_scopes;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * EXAMPLE 3 — Bean scopes: singleton (shared) vs prototype (fresh copy)
 *
 * Story:
 *   - The CLASS REGISTER is one book everyone shares    -> singleton (default)
 *   - Every student gets their OWN EXAM PAPER           -> prototype
 */
public class ScopeExample {

    @Component                                  // singleton by default
    static class ClassRegister {
    }

    @Component
    @Scope("prototype")                         // a new one every time someone asks
    static class ExamPaper {
    }

    @Configuration
    @ComponentScan
    static class Config {
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {

            ClassRegister registerA = context.getBean(ClassRegister.class);
            ClassRegister registerB = context.getBean(ClassRegister.class);
            System.out.println("Register asked twice. Same object?   " + (registerA == registerB) + "  (singleton)");

            ExamPaper paperA = context.getBean(ExamPaper.class);
            ExamPaper paperB = context.getBean(ExamPaper.class);
            System.out.println("Exam paper asked twice. Same object? " + (paperA == paperB) + " (prototype)");
        }
    }
}
