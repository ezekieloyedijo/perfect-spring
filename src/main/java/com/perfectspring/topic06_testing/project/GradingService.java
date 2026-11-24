package com.perfectspring.topic06_testing.project;

import org.springframework.stereotype.Service;

/**
 * The business rules we want to PROVE with tests.
 *   A >= 70, B >= 60, C >= 50, D >= 45, E >= 40, F < 40.   Pass = 40 or more.
 */
@Service
public class GradingService {

    private final ScoreRepository repository;

    public GradingService(ScoreRepository repository) {      // constructor injection = easy to test
        this.repository = repository;
    }

    public ResultCard resultFor(Long studentId) {
        StudentScores student = repository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        double average = student.scores().stream().mapToInt(Integer::intValue).average().orElse(0);
        return new ResultCard(student.studentId(), student.name(), average, gradeFor(average), average >= 40);
    }

    public String gradeFor(double average) {
        if (average >= 70) return "A";
        if (average >= 60) return "B";
        if (average >= 50) return "C";
        if (average >= 45) return "D";
        if (average >= 40) return "E";
        return "F";
    }
}
