package com.perfectspring.topic06_testing.project;

import java.util.Optional;

/** An interface, so tests can replace the real storage with a mock. */
public interface ScoreRepository {

    Optional<StudentScores> findById(Long studentId);
}
