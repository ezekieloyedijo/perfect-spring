package com.perfectspring.topic07_boot.project;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * What a client SENDS to create a course, plus the FORM-CHECKER rules.
 * With @Valid in the controller, bad data gets a 400 before our service even runs.
 *
 * capacity is an Integer (not int) so it may be left out. Then the school default is used.
 */
public record CourseRequest(
        @NotBlank(message = "code is required")
        @Size(max = 10, message = "code must be at most 10 characters")
        String code,

        @NotBlank(message = "title is required")
        String title,

        String teacher,

        @Min(value = 1, message = "capacity must be at least 1")
        @Max(value = 200, message = "capacity must be at most 200")
        Integer capacity
) {
}
