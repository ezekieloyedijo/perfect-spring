package com.perfectspring.topic02_core.example.ex2_qualifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * EXAMPLE 2 — Two beans fit the same job: @Primary and @Qualifier
 *
 * Story: two Maths teachers work here.
 *   - Mrs. Bello is HEAD of Maths (@Primary) -> sent by default.
 *   - Mr. Okafor is requested BY NAME (@Qualifier) by the Further-Maths class.
 *
 * Experiment: delete @Primary below and run again. Read the error message!
 */
public class QualifierExample {

    interface MathsTeacher {
        String name();
    }

    @Component
    @Primary                                   // "use me if nobody asks for someone specific"
    static class MrsBello implements MathsTeacher {
        public String name() { return "Mrs. Bello (Head of Maths)"; }
    }

    @Component("mrOkafor")                     // bean name = "mrOkafor"
    static class MrOkafor implements MathsTeacher {
        public String name() { return "Mr. Okafor"; }
    }

    @Component
    static class Jss1Class {
        private final MathsTeacher teacher;
        Jss1Class(MathsTeacher teacher) {      // no preference -> gets the @Primary one
            this.teacher = teacher;
        }
        void report() { System.out.println("JSS1 Maths is taught by:          " + teacher.name()); }
    }

    @Component
    static class FurtherMathsClass {
        private final MathsTeacher teacher;
        FurtherMathsClass(@Qualifier("mrOkafor") MathsTeacher teacher) {  // asks BY NAME
            this.teacher = teacher;
        }
        void report() { System.out.println("Further Maths is taught by:       " + teacher.name()); }
    }

    @Configuration
    @ComponentScan
    static class Config {
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            context.getBean(Jss1Class.class).report();
            context.getBean(FurtherMathsClass.class).report();
        }
    }
}
