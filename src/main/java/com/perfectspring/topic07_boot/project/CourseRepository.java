package com.perfectspring.topic07_boot.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * THE SMART RECORDS ASSISTANT. It's only an interface: Spring writes the class at start-up.
 *
 * Free methods from JpaRepository<Course, Long>:
 *   save, saveAll, findAll, findById, existsById, count, deleteById, delete, ... (and paging/sorting)
 *
 * Derived queries: Spring reads the METHOD NAME and writes the SQL.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

    // SELECT * FROM course WHERE UPPER(title) LIKE UPPER('%' || ? || '%')
    List<Course> findByTitleContainingIgnoreCase(String text);

    // SELECT COUNT(*) > 0 FROM course WHERE code = ?
    boolean existsByCode(String code);

    // TASK (Lesson Part C, 18): add   List<Course> findByTeacher(String teacher);
}
