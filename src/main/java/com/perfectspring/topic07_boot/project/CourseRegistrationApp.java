package com.perfectspring.topic07_boot.project;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * PROJECT 7 — Course Registration API (port 8087)
 *
 * This ONE class replaces Topic 4's TomcatLauncher + WebConfig + Topic 3's DatabaseConfig.
 *
 * @SpringBootApplication       -> @Configuration + @ComponentScan + @EnableAutoConfiguration
 * @ConfigurationPropertiesScan -> find @ConfigurationProperties classes (SchoolProperties)
 * profiles("boot")             -> also load application-boot.properties
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class CourseRegistrationApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(CourseRegistrationApp.class)
                .profiles("boot")
                .run(args);
    }
}
