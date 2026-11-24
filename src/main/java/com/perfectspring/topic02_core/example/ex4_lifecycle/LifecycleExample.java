package com.perfectspring.topic02_core.example.ex4_lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * EXAMPLE 4 — The life of a bean
 *
 *   1. constructor runs        (hired)
 *   2. dependencies injected   (given the keys)
 *   3. @PostConstruct runs     (orientation: arrange the shelves)
 *   4. bean is used            (works all term)
 *   5. @PreDestroy runs        (hand back keys, lock up) when the container closes
 */
public class LifecycleExample {

    @Component
    static class Keys {
    }

    @Component
    static class Librarian {
        private final Keys keys;

        Librarian(Keys keys) {
            this.keys = keys;
            System.out.println("1+2. Librarian hired and given the keys (constructor + injection)");
        }

        @PostConstruct
        void orientation() {
            System.out.println("3.   @PostConstruct: shelves arranged, library is OPEN");
        }

        void work() {
            System.out.println("4.   Librarian is lending books...");
        }

        @PreDestroy
        void lastDay() {
            System.out.println("5.   @PreDestroy: lights off, keys handed back, library CLOSED");
        }
    }

    @Configuration
    @ComponentScan
    static class Config {
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            context.getBean(Librarian.class).work();
            System.out.println("     (main is finishing: the container is about to close)");
        } // <- the container closes here, which triggers @PreDestroy
    }
}
