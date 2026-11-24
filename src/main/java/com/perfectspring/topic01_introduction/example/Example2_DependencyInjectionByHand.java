package com.perfectspring.topic01_introduction.example;

/**
 * EXAMPLE 2 — THE FIX, DONE BY HAND: INTERFACE + DEPENDENCY INJECTION (still no Spring)
 *
 * Story: the Car now has a "USB-C port" (the Engine interface).
 * Somebody OUTSIDE the car (the kit manager, here: main) chooses an engine
 * and HANDS it to the car through the constructor. That handing-in is "injection".
 */
public class Example2_DependencyInjectionByHand {

    // The "port shape": anything that can start() is an Engine.
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
        private final Engine engine;          // ✅ depends on the SHAPE, not a specific engine

        Car(Engine engine) {                  // ✅ "Please HAND me an engine" = constructor injection
            this.engine = engine;
        }

        void drive() {
            engine.start();
            System.out.println("The car is moving.");
        }
    }

    public static void main(String[] args) {
        // WE are the "HR office" here. We create the parts and plug them together.
        Car petrolCar = new Car(new PetrolEngine());
        petrolCar.drive();

        System.out.println();

        Car electricCar = new Car(new ElectricEngine());   // same Car class, different engine!
        electricCar.drive();

        System.out.println();
        System.out.println("Car never changed. Only what we HANDED to it changed.");
        System.out.println("But imagine doing this wiring by hand for 300 classes... See Example 3.");
    }
}
