package com.perfectspring.topic03_dataaccess.project;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PROJECT 3 — Student Records
 *
 * Shows full CRUD with JdbcTemplate, then an all-or-nothing batch with @Transactional.
 */
public class StudentRecordsApp {

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(DatabaseConfig.class)) {
            StudentRepository repository = context.getBean(StudentRepository.class);
            StudentService service = context.getBean(StudentService.class);

            printAll("1) Starting data (from data.sql):", repository);

            // CREATE
            repository.save("Zainab", "JSS1", 79);
            printAll("2) After INSERT of Zainab:", repository);

            // READ (filtered + by id)
            System.out.println("3) Only JSS1 students:");
            repository.findByClass("JSS1").forEach(s -> System.out.println("   " + s));
            System.out.println("   Student #3 is: " + repository.findById(3).map(Student::getName).orElse("nobody"));
            System.out.println("   Student #99 is: " + repository.findById(99).map(Student::getName).orElse("nobody"));
            System.out.println();

            // UPDATE + DELETE
            repository.updateScore(2, 80);
            repository.deleteById(4);
            printAll("4) After UPDATE Tunde -> 80 and DELETE Musa:", repository);

            // TRANSACTION: a batch with one BAD score (150)
            System.out.println("5) Recording a batch of exam results where one score is invalid...");
            Map<Long, Integer> batch = new LinkedHashMap<>();
            batch.put(1L, 99);
            batch.put(2L, 98);
            batch.put(3L, 150);      // TASK: change 150 to 95 and run again
            try {
                service.recordExamResults(batch);
            } catch (IllegalArgumentException e) {
                System.out.println("   ERROR: " + e.getMessage() + " -> whole batch ROLLED BACK");
            }
            printAll("6) After the batch (Ada and Tunde keep their OLD scores):", repository);
        }
    }

    private static void printAll(String title, StudentRepository repository) {
        System.out.println(title + "  [" + repository.count() + " students]");
        repository.findAll().forEach(s -> System.out.println("   " + s));
        System.out.println();
    }
}
