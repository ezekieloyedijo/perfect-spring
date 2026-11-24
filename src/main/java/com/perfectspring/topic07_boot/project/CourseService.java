package com.perfectspring.topic07_boot.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business rules. Uses the repository (JPA) and the typed settings (SchoolProperties).
 *
 * Note: no @EnableTransactionManagement and no TransactionManager bean anywhere in this
 * project. Spring Boot auto-configured them (compare with Topic 3's DatabaseConfig).
 */
@Service
public class CourseService {

    private final CourseRepository repository;
    private final SchoolProperties school;

    public CourseService(CourseRepository repository, SchoolProperties school) {
        this.repository = repository;
        this.school = school;
    }

    public List<Course> findAll() {
        return repository.findAll();
    }

    public Course findOne(Long id) {
        return repository.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
    }

    public List<Course> searchByTitle(String text) {
        return repository.findByTitleContainingIgnoreCase(text);
    }

    @Transactional
    public Course create(CourseRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new DuplicateCourseCodeException(request.code());
        }
        int capacity = request.capacity() != null ? request.capacity() : school.defaultCourseCapacity();
        return repository.save(new Course(request.code(), request.title(), request.teacher(), capacity));
    }

    /**
     * DIRTY CHECKING: we load the course and change it, and we never call save().
     * At the end of this @Transactional method, JPA notices the change and writes the UPDATE itself.
     */
    @Transactional
    public Course enroll(Long courseId) {
        Course course = findOne(courseId);
        if (course.isFull()) {
            throw new CourseFullException("Course " + course.getCode() + " is full (" + course.getCapacity() + " seats)");
        }
        course.enrollOneStudent();
        return course;
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new CourseNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
