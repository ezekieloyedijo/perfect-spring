package com.perfectspring.topic05_security.project;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory storage of results: student username -> list of "Subject: score". */
@Service
public class ResultBook {

    private final Map<String, List<String>> results = new ConcurrentHashMap<>();

    public ResultBook() {
        add("ada", "Maths", 88);
        add("ada", "English", 75);
        add("tunde", "Maths", 64);
    }

    public void add(String student, String subject, int score) {
        results.computeIfAbsent(student, s -> new ArrayList<>()).add(subject + ": " + score);
    }

    public List<String> resultsOf(String student) {
        return results.getOrDefault(student, List.of());
    }

    public Map<String, List<String>> all() {
        return results;
    }

    /**
     * The EXTRA LOCK on the principal's office door.
     * Even if someone forgot the URL rule, this method itself demands an ADMIN.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void clearAll() {
        results.clear();
    }
}
