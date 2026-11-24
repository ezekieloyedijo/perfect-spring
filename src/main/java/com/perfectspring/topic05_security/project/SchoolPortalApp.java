package com.perfectspring.topic05_security.project;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * PROJECT 5 — School Portal (port 8085)
 *
 * Spring Boot starts Tomcat + Spring MVC + Spring Security for us.
 * (Topic 7 explains how. For now: "the button that starts everything".)
 *
 * profiles("security") -> also load application-security.properties (port 8085).
 */
@SpringBootApplication
public class SchoolPortalApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SchoolPortalApp.class)
                .profiles("security")
                .run(args);
    }
}
