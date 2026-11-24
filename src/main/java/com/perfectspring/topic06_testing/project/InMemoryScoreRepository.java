package com.perfectspring.topic06_testing.project;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** The REAL storage used when the app runs (and in the full integration test). */
@Repository
public class InMemoryScoreRepository implements ScoreRepository {

    private final Map<Long, StudentScores> data = Map.of(
            1L, new StudentScores(1L, "Ada", List.of(80, 90)),        // average 85 -> A
            2L, new StudentScores(2L, "Tunde", List.of(50, 60)),      // average 55 -> C
            3L, new StudentScores(3L, "Musa", List.of(30, 36)));      // average 33 -> F

    @Override
    public Optional<StudentScores> findById(Long studentId) {
        return Optional.ofNullable(data.get(studentId));
    }
}
