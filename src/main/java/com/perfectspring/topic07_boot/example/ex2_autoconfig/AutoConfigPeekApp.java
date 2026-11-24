package com.perfectspring.topic07_boot.example.ex2_autoconfig;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

/**
 * EXAMPLE 2 — PEEK AT AUTO-CONFIGURATION
 *
 * This class defines ZERO beans of its own. Yet after start-up the container holds
 * hundreds of beans. Spring Boot's auto-configuration created them, because it saw
 * the matching libraries on the classpath.
 *
 * (This app is not a web server, so it starts, prints and stops.)
 */
@SpringBootApplication
public class AutoConfigPeekApp {

    public static void main(String[] args) {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(AutoConfigPeekApp.class)
                .web(WebApplicationType.NONE)        // don't start Tomcat for this demo
                .run(args)) {

            System.out.println();
            System.out.println("Beans I wrote myself:      0");
            System.out.println("Beans in the container:    " + context.getBeanDefinitionCount() + "  <- Spring Boot made these");
            System.out.println();
            System.out.println("Did Boot create these for me? (I never wrote any of them)");

            List<String> typesToCheck = List.of(
                    "javax.sql.DataSource",                                    // because H2 is on the classpath
                    "org.springframework.jdbc.core.JdbcTemplate",              // because there's a DataSource
                    "org.springframework.transaction.PlatformTransactionManager",
                    "jakarta.persistence.EntityManagerFactory",                // because Hibernate is there
                    "com.fasterxml.jackson.databind.ObjectMapper",             // because Jackson is there
                    "jakarta.validation.Validator");                           // because validation is there

            for (String typeName : typesToCheck) {
                try {
                    Class<?> type = Class.forName(typeName);
                    String[] names = context.getBeanNamesForType(type);
                    System.out.printf("  %-60s %s%n", typeName, names.length > 0 ? "YES (bean: " + names[0] + ")" : "no");
                } catch (ClassNotFoundException e) {
                    System.out.printf("  %-60s library not on classpath%n", typeName);
                }
            }

            System.out.println();
            System.out.println("Database URL Boot chose:   "
                    + context.getEnvironment().getProperty("spring.datasource.url", "(generated in-memory H2 URL)"));
        }
    }
}
