package com.perfectspring.topic04_webmvc.example;

import com.perfectspring.common.JsonOnlyWebConfig;
import com.perfectspring.topic04_webmvc.server.TomcatLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Map;

/**
 * EXAMPLE 2 — THE SAME THING WITH SPRING MVC
 *
 * Try:
 *   Browser:  http://localhost:8042/hello?name=Ada
 *   Browser:  http://localhost:8042/students/1
 *   Browser:  http://localhost:8042/students/99      (404)
 *   POST:     http://localhost:8042/echo   with body {"name":"Chioma","age":17}
 *             (use the topic04 homework-api.http file, or Postman)
 *
 * Compare with Example 1: no if/else routing, no URL cutting, no hand-written JSON.
 */
public class Example2_HelloController {

    /** A simple data class. Jackson turns it to/from JSON. */
    public record Student(long id, String name) {
    }

    /** What the client sends to /echo. */
    public record Greeting(String name, int age) {
    }

    @RestController                                     // "I answer web requests. Return values become JSON."
    public static class HelloController {

        private final Map<Long, Student> students = Map.of(
                1L, new Student(1, "Ada"),
                2L, new Student(2, "Tunde"));

        // GET /hello?name=Ada
        @GetMapping("/hello")
        public Map<String, String> hello(@RequestParam(value = "name", defaultValue = "stranger") String name) {
            return Map.of("message", "Hello, " + name + "!");          // Map -> JSON automatically
        }

        // GET /students/1
        @GetMapping("/students/{id}")
        public ResponseEntity<Student> student(@PathVariable("id") long id) {   // "1" -> long, automatically
            Student found = students.get(id);
            if (found == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();     // 404
            }
            return ResponseEntity.ok(found);                                     // 200 + JSON
        }

        // POST /echo   body: {"name":"Chioma","age":17}
        @PostMapping("/echo")
        public Map<String, Object> echo(@RequestBody Greeting greeting) {       // JSON -> Java, automatically
            return Map.of(
                    "youSent", greeting,
                    "reply", "Nice to meet you " + greeting.name() + ", in 5 years you'll be " + (greeting.age() + 5));
        }
    }

    /** Switch on Spring MVC and register our controller. */
    @Configuration
    @EnableWebMvc                       // turns on Spring MVC (mappings, JSON converters, ...)
    @Import({HelloController.class, JsonOnlyWebConfig.class})   // JsonOnlyWebConfig: answer in JSON only
    static class WebConfig {
    }

    public static void main(String[] args) throws Exception {
        TomcatLauncher.startSpringMvc(8042, WebConfig.class);
    }
}
