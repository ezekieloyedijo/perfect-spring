package com.perfectspring.topic06_testing.project;

/** What the API returns: the calculated result for one student. */
public record ResultCard(Long studentId, String name, double average, String grade, boolean passed) {
}
