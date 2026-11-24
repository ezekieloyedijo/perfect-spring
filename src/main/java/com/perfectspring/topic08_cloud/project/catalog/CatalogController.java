package com.perfectspring.topic08_cloud.project.catalog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;

/**
 * The catalog OWNS the course data. No other service touches it directly;
 * they must ASK through this API.
 */
@RestController
@RequestMapping("/catalog/courses")
public class CatalogController {

    /** What we send back. servedByCatalogPort shows WHICH copy answered (load balancing!). */
    public record CatalogCourse(String code, String title, String teacher, int servedByCatalogPort) {
    }

    private final int myPort;
    private final Map<String, String[]> courses = Map.of(
            "CS101", new String[]{"Intro to Java", "Mrs. Bello"},
            "CS102", new String[]{"Spring Boot Basics", "Mr. Okafor"},
            "CLOUD1", new String[]{"Microservices 101", "Ms. Ade"});

    public CatalogController(@Value("${server.port}") int myPort) {
        this.myPort = myPort;
    }

    @GetMapping
    public Collection<CatalogCourse> all() {
        return courses.keySet().stream().sorted().map(this::toCourse).toList();
    }

    @GetMapping("/{code}")
    public ResponseEntity<CatalogCourse> one(@PathVariable("code") String code) {
        String key = code.toUpperCase();
        if (!courses.containsKey(key)) {
            return ResponseEntity.notFound().build();          // 404
        }
        System.out.println("[catalog :" + myPort + "] someone asked for " + key);
        return ResponseEntity.ok(toCourse(key));
    }

    private CatalogCourse toCourse(String code) {
        String[] info = courses.get(code);
        return new CatalogCourse(code, info[0], info[1], myPort);
    }
}
