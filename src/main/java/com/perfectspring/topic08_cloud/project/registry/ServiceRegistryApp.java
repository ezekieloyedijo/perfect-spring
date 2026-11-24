package com.perfectspring.topic08_cloud.project.registry;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * APP 1 of 3 — THE SERVICE REGISTRY (port 8761). Start this FIRST.
 *
 * Story: the campus PHONE DIRECTORY. Services write themselves in and keep saying
 * "still here!" (heartbeats). Others look services up by NAME.
 *
 * Dashboard: http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer               // this whole app IS the phone directory. One annotation!
public class ServiceRegistryApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ServiceRegistryApp.class)
                .profiles("registry")
                .run(args);
    }
}
