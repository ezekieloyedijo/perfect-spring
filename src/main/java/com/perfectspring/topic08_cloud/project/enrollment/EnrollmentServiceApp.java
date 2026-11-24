package com.perfectspring.topic08_cloud.project.enrollment;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * APP 3 of 3 — THE ENROLLMENT SERVICE (port 8082)
 *
 * It registers itself in Eureka AND uses Eureka to find "catalog-service".
 *
 * @EnableFeignClients -> find @FeignClient interfaces (CatalogClient) and generate their HTTP code.
 */
@SpringBootApplication
@EnableFeignClients
public class EnrollmentServiceApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(EnrollmentServiceApp.class)
                .profiles("enrollment")
                .run(args);
    }
}
