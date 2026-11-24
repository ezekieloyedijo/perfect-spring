package com.perfectspring.topic01_introduction.example;

/**
 * EXAMPLE 1 — THE PROBLEM: TIGHT COUPLING (no Spring yet)
 *
 * Story: a Car that builds its OWN engine inside itself.
 * Like a phone with the charger cable WELDED on.
 *
 * Try this: change the car to use an ElectricEngine.
 * Notice you are forced to EDIT the Car class itself. That's the problem.
 */
public class Example1_TightCoupling {

    static class PetrolEngine {
        void start() {
            System.out.println("Vroom! Petrol engine started.");
        }
    }

    static class ElectricEngine {
        void start() {
            System.out.println("Hmmmm... Electric engine started silently.");
        }
    }

    static class Car {
        // ❌ The Car creates ("hires") its own engine with `new`.
        //    It is now welded to PetrolEngine forever.
        private final PetrolEngine engine = new PetrolEngine();

        void drive() {
            engine.start();
            System.out.println("The car is moving.");
        }
    }

    public static void main(String[] args) {
        Car car = new Car();
        car.drive();

        System.out.println();
        System.out.println("Question: how do we make this car electric WITHOUT editing class Car?");
        System.out.println("Answer: we can't! That's tight coupling. See Example 2.");
    }
}
