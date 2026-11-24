package com.perfectspring.topic06_testing.example;

import java.util.List;

/**
 * Has a DEPENDENCY (ScoreSource), given through the constructor.
 * That's what lets a test hand in a MOCK instead of a real database.
 */
public class ReportCardService {

    private final ScoreSource scoreSource;
    private final ScoreCalculator calculator = new ScoreCalculator();

    public ReportCardService(ScoreSource scoreSource) {
        this.scoreSource = scoreSource;
    }

    public String reportFor(String student) {
        List<Integer> scores = scoreSource.scoresFor(student);
        if (scores.isEmpty()) {
            return student + ": no scores yet";
        }
        double average = calculator.average(scores);
        return student + ": average " + average + ", grade " + calculator.grade(average);
    }
}
