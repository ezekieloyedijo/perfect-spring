package com.perfectspring.topic07_boot.example.ex1_hello;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * EXAMPLE 1 — A COMPLETE WEB APP IN ONE SMALL FILE
 *
 * Compare with Topic 4: no TomcatLauncher, no DispatcherServlet, no @EnableWebMvc.
 * Spring Boot's auto-configuration sees Spring MVC + Tomcat on the classpath and sets them up.
 *
 * Try: http://localhost:8070/hello?name=Ada
 */
@SpringBootApplication          // = @Configuration + @ComponentScan + @EnableAutoConfiguration
@RestController                 // this same class is also a controller (fine for a tiny demo)
public class HelloBootApp {

    @GetMapping("/hello")
    public Map<String, String> hello(@RequestParam(value = "name", defaultValue = "world") String name) {
        return Map.of("message", "Hello " + name + ", from Spring Boot!");
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(HelloBootApp.class)
                .properties("server.port=8070")      // one setting. Everything else is a sensible default
                .run(args);
    }
}
