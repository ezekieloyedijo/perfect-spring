package com.perfectspring.topic06_testing.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/** A small Spring config used by Example3_SpringContextTest (a plain-Spring integration test). */
@Configuration
public class ExampleConfig {

    @Bean
    public ScoreSource scoreSource() {
        Map<String, List<Integer>> data = Map.of(
                "ada", List.of(70, 80, 90),
                "tunde", List.of(40, 50));
        return student -> data.getOrDefault(student, List.of());   // a tiny in-memory ScoreSource
    }

    @Bean
    public ReportCardService reportCardService(ScoreSource scoreSource) {
        return new ReportCardService(scoreSource);
    }
}
