package com.perfectspring.topic08_cloud.project.enrollment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * POST /enrollments  {"student":"ada","courseCode":"CS101"}
 *
 * Before saving, we ASK THE CATALOG SERVICE (another app!) whether the course exists.
 * The call looks like a normal Java method call: catalogClient.findCourse(...)
 */
@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    public record EnrollmentRequest(String student, String courseCode) {
    }

    public record Enrollment(long id, String student, String courseCode, String courseTitle,
                             String status, int servedByCatalogPort) {
    }

    private final CatalogClient catalogClient;
    private final List<Enrollment> enrollments = new CopyOnWriteArrayList<>();   // this service's OWN data
    private final AtomicLong nextId = new AtomicLong(1);

    public EnrollmentController(CatalogClient catalogClient) {
        this.catalogClient = catalogClient;
    }

    @PostMapping
    public ResponseEntity<?> enroll(@RequestBody EnrollmentRequest request) {
        CatalogCourse course = catalogClient.findCourse(request.courseCode());   // 👈 a call to ANOTHER app

        if (course == null) {                                                     // catalog said 404
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No such course in the catalog: " + request.courseCode()));
        }

        boolean catalogWasDown = CatalogClientFallback.UNAVAILABLE.equals(course.title());
        Enrollment enrollment = new Enrollment(
                nextId.getAndIncrement(),
                request.student(),
                request.courseCode().toUpperCase(),
                catalogWasDown ? "(unknown - catalog unavailable)" : course.title(),
                catalogWasDown ? "PENDING" : "CONFIRMED",       // the fallback keeps us working
                course.servedByCatalogPort());

        enrollments.add(enrollment);
        return ResponseEntity.status(catalogWasDown ? HttpStatus.ACCEPTED : HttpStatus.CREATED).body(enrollment);
    }

    @GetMapping
    public List<Enrollment> all() {
        return enrollments;
    }
}
