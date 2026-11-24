package com.perfectspring.topic07_boot.project;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Exactly what you learned in Topic 4. Boot only removed the set-up around it. */
@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public List<Course> all() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Course one(@PathVariable("id") Long id) {
        return service.findOne(id);
    }

    @GetMapping("/search")
    public List<Course> search(@RequestParam("title") String title) {
        return service.searchByTitle(title);
    }

    @PostMapping
    public ResponseEntity<Course> create(@Valid @RequestBody CourseRequest request) {   // @Valid = run the form checker
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PostMapping("/{id}/enroll")
    public Course enroll(@PathVariable("id") Long id) {
        return service.enroll(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
