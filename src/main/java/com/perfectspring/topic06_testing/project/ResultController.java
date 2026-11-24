package com.perfectspring.topic06_testing.project;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** GET /results/{studentId} -> the student's result card as JSON. */
@RestController
public class ResultController {

    private final GradingService gradingService;

    public ResultController(GradingService gradingService) {
        this.gradingService = gradingService;
    }

    @GetMapping("/results/{studentId}")
    public ResultCard result(@PathVariable("studentId") Long studentId) {
        return gradingService.resultFor(studentId);
    }
}
