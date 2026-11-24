package com.perfectspring.topic03_dataaccess.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * The BUSINESS RULES. It uses the repository, but never writes SQL itself.
 */
@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    /**
     * Records a whole batch of exam results: ALL or NOTHING.
     *
     * If even one score is invalid, an exception is thrown and Spring ROLLS BACK
     * every score already updated in this batch. The register is never half-updated.
     *
     * TASK: remove @Transactional and run the app again. Watch the good scores stay changed!
     */
    @Transactional
    public void recordExamResults(Map<Long, Integer> scoresByStudentId) {
        scoresByStudentId.forEach((studentId, score) -> {
            if (score < 0 || score > 100) {
                throw new IllegalArgumentException("Invalid score " + score + " for student #" + studentId);
            }
            repository.updateScore(studentId, score);
            System.out.println("   updated student #" + studentId + " -> " + score);
        });
    }
}
