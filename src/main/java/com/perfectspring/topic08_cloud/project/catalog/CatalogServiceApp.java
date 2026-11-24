package com.perfectspring.topic08_cloud.project.catalog;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * APP 2 of 3 — THE COURSE CATALOG SERVICE (port 8081)
 *
 * A normal Spring Boot app (Topic 7). Because the Eureka client library is present and
 * enabled in application-catalog.properties, it REGISTERS ITSELF as "catalog-service".
 *
 * Second copy for load balancing:
 *   mvnw spring-boot:run -Dstart-class=com.perfectspring.topic08_cloud.project.catalog.CatalogServiceApp -Dspring-boot.run.arguments=--server.port=8091
 */
@SpringBootApplication
public class CatalogServiceApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(CatalogServiceApp.class)
                .profiles("catalog")
                .run(args);
    }
}
