package com.perfectspring.topic01_introduction.example;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EXAMPLE 3 — LET SPRING DO THE WIRING (Inversion of Control)
 *
 * Story: we hire a professional HR office (the Spring container).
 * We write a "hiring plan" (@Configuration class).
 * Spring reads the plan, creates the objects (beans) and plugs them together.
 */
public class Example3_SpringDoesTheWiring {

    interface Engine {
        void start();
    }

    static class PetrolEngine implements Engine {
        public void start() {
            System.out.println("Vroom! Petrol engine started.");
        }
    }

    static class ElectricEngine implements Engine {
        public void start() {
            System.out.println("Hmmmm... Electric engine started silently.");
        }
    }

    static class Car {
        private final Engine engine;

        Car(Engine engine) {
            this.engine = engine;
        }

        void drive() {
            engine.start();
            System.out.println("The car is moving.");
        }
    }

    /** The HIRING PLAN. Spring reads this class and follows the instructions. */
    @Configuration
    static class CarFactoryConfig {

        @Bean                       // "Spring, create an Engine and keep it on your staff list"
        Engine engine() {
            return new ElectricEngine();      // 👈 change this ONE line to switch engines
        }

        @Bean                       // "Spring, create a Car. It needs an Engine, so plug in the one above"
        Car car(Engine engine) {
            return new Car(engine);
        }
    }

    public static void main(String[] args) {
        // 1. Open the HR office (start the Spring container) and give it the hiring plan.
        //    try-with-resources closes the container neatly at the end.
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(CarFactoryConfig.class)) {

            // 2. Ask HR for a ready, fully-assembled Car. We never wrote `new Car(...)` here!
            Car car = context.getBean(Car.class);
            car.drive();

            // 3. Peek at HR's staff list: every bean Spring is managing.
            System.out.println();
            System.out.println("Beans on Spring's staff list:");
            for (String name : context.getBeanDefinitionNames()) {
                if (!name.startsWith("org.springframework")) {
                    System.out.println("  - " + name);
                }
            }
        }
    }
}
