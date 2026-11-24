package com.perfectspring.topic06_testing.example;

import java.util.List;

/**
 * Pure logic with NO dependencies: the easiest kind of class to test.
 * Its tests live in src/test/java/.../topic06_testing/example/Example1_FirstJUnitTest.java
 */
public class ScoreCalculator {

    public double average(List<Integer> scores) {
        if (scores == null || scores.isEmpty()) {
            throw new IllegalArgumentException("At least one score is needed");
        }
        return scores.stream().mapToInt(Integer::intValue).average().orElseThrow();
    }

    public String grade(double score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100");
        }
        if (score >= 70) return "A";
        if (score >= 60) return "B";
        if (score >= 50) return "C";
        if (score >= 45) return "D";
        if (score >= 40) return "E";
        return "F";
    }
}
