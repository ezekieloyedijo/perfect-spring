package com.perfectspring.topic05_security.project;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Notice: NO password checks or role checks in here!
 * The security filter chain already did that BEFORE these methods run.
 */
@RestController
public class PortalController {

    private final ResultBook resultBook;

    public PortalController(ResultBook resultBook) {
        this.resultBook = resultBook;
    }

    /** What a teacher sends to record a score. */
    public record NewResult(String student, String subject, int score) {
    }

    // ---------- anyone ----------
    @GetMapping("/public/notice-board")
    public Map<String, String> noticeBoard() {
        return Map.of("notice", "Inter-house sports is on Friday. Everyone is welcome!");
    }

    // ---------- any logged-in user ----------
    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {          // the verified "visitor badge"
        List<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return Map.of("username", auth.getName(), "roles", roles);
    }

    // ---------- STUDENT ----------
    @GetMapping("/results/my")
    public Map<String, Object> myResults(Authentication auth) {
        // We use auth.getName(), NOT a name from the request, so a student can only see THEIR results.
        return Map.of("student", auth.getName(), "results", resultBook.resultsOf(auth.getName()));
    }

    // ---------- TEACHER ----------
    @PostMapping("/results")
    public ResponseEntity<Map<String, String>> addResult(@RequestBody NewResult result, Authentication auth) {
        resultBook.add(result.student(), result.subject(), result.score());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("saved", result.student() + " - " + result.subject() + ": " + result.score(),
                             "recordedBy", auth.getName()));
    }

    // ---------- ADMIN ----------
    @GetMapping("/admin/users")
    public List<String> users() {
        return List.of("ada (STUDENT)", "tunde (STUDENT)", "mrbello (TEACHER)", "admin (ADMIN)");
    }

    @DeleteMapping("/admin/results")
    public ResponseEntity<Void> clearResults() {
        resultBook.clearAll();                                    // also protected by @PreAuthorize
        return ResponseEntity.noContent().build();
    }
}
