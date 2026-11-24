package com.perfectspring.topic07_boot.project;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * A PRINTED SETTINGS CARD: every "school.*" setting from application-boot.properties
 * in one typed object (instead of many @Value annotations).
 *
 *   school.name=Unity High School          -> name()
 *   school.default-course-capacity=30      -> defaultCourseCapacity()   (dashes -> camelCase)
 */
@ConfigurationProperties(prefix = "school")
public record SchoolProperties(String name, int defaultCourseCapacity) {
}
