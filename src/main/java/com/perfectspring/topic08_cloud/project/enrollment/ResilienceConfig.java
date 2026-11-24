package com.perfectspring.topic08_cloud.project.enrollment;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * THE BREAKER'S RULES (Resilience4j), written out so students can read and change them.
 */
@Configuration
public class ResilienceConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> circuitBreakerRules() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(3))         // slower than 3 s counts as a failure
                        .build())
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(4)                            // look at the last 4 calls...
                        .minimumNumberOfCalls(2)                         // ...once there have been at least 2
                        .failureRateThreshold(50)                        // 50% or more failed -> OPEN (trip!)
                        .waitDurationInOpenState(Duration.ofSeconds(10)) // rest 10 s, then HALF-OPEN
                        .permittedNumberOfCallsInHalfOpenState(1)        // 1 test call decides CLOSED or OPEN
                        .build())
                .build());
    }
}
