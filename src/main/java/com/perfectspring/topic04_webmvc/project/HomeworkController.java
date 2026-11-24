package com.perfectspring.topic04_webmvc.project;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * FRONT OFFICE layer: speaks HTTP and nothing else.
 * Every method is THIN: read the request, call the service, return the result.
 *
 *   GET    /homework              -> list (optional ?subject=Maths)
 *   GET    /homework/{id}         -> one
 *   POST   /homework              -> create   (201)
 *   PUT    /homework/{id}/done    -> mark done
 *   DELETE /homework/{id}         -> delete   (204)
 */
@RestController
@RequestMapping("/homework")
public class HomeworkController {

    private final HomeworkService service;

    public HomeworkController(HomeworkService service) {
        this.service = service;
    }

    @GetMapping
    public List<Homework> all(@RequestParam(value = "subject", required = false) String subject) {
        return service.findAll(subject);                                    // 200 OK + JSON list
    }

    @GetMapping("/{id}")
    public Homework one(@PathVariable("id") long id) {
        return service.findOne(id);                                          // 200 OK (or 404 via GlobalErrorHandler)
    }

    @PostMapping
    public ResponseEntity<Homework> create(@RequestBody Homework homework) {
        Homework saved = service.create(homework);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);       // 201 Created
    }

    @PutMapping("/{id}/done")
    public Homework markDone(@PathVariable("id") long id) {
        return service.markDone(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();                          // 204 No Content
    }
}
