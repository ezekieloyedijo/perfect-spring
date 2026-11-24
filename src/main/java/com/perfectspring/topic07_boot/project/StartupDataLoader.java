package com.perfectspring.topic07_boot.project;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OPENING-DAY TASKS: Boot calls run(...) once, right after the app has started.
 * Every bean is ready by then, so using the repository here is safe.
 */
@Component
public class StartupDataLoader implements CommandLineRunner {

    private final CourseRepository repository;
    private final SchoolProperties school;

    public StartupDataLoader(CourseRepository repository, SchoolProperties school) {
        this.repository = repository;
        this.school = school;
    }

    @Override
    public void run(String... args) {
        repository.saveAll(List.of(
                new Course("CS101", "Intro to Java", "Mrs. Bello", 30),
                new Course("CS102", "Spring Boot Basics", "Mr. Okafor", 25),
                new Course("ROB1", "Robotics Club", "Ms. Ade", 2)));

        System.out.println();
        System.out.println("=====================================================");
        System.out.println(" " + school.name() + " - Course Registration API");
        System.out.println(" Loaded " + repository.count() + " courses.  Try: http://localhost:8087/courses");
        System.out.println("=====================================================");
    }
}
