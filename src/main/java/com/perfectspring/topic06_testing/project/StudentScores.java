package com.perfectspring.topic06_testing.project;

import java.util.List;

/** A student and their raw scores (what the repository stores). */
public record StudentScores(Long studentId, String name, List<Integer> scores) {
}
