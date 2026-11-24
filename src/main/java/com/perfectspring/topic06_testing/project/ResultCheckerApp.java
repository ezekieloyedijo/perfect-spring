package com.perfectspring.topic06_testing.project;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * PROJECT 6 — Result Checker API (port 8086)
 *
 * Note: Spring Security is on the classpath (for Topic 5), but it's switched off for this
 * app in src/main/resources/application.properties (spring.autoconfigure.exclude).
 * Topic 7 explains auto-configuration.
 */
@SpringBootApplication
public class ResultCheckerApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ResultCheckerApp.class)
                .profiles("testing")
                .run(args);
    }
}
