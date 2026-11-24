package com.perfectspring.topic06_testing.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EXAMPLE 3 — A SPRING INTEGRATION TEST (plain Spring, like Topics 1-3)
 *
 * @SpringJUnitConfig starts a REAL Spring container with ExampleConfig,
 * then @Autowired injects real beans INTO the test.
 * This proves the WIRING works, not just the logic.
 */
@SpringJUnitConfig(ExampleConfig.class)
class Example3_SpringContextTest {

    @Autowired
    ReportCardService service;               // a real bean, created and wired by Spring

    @Autowired
    ApplicationContext context;

    @Test
    void springCreatesAndWiresTheService() {
        assertThat(service).isNotNull();
        assertThat(service.reportFor("ada")).isEqualTo("ada: average 80.0, grade A");
    }

    @Test
    void reportCardServiceIsASingleton() {
        ReportCardService first = context.getBean(ReportCardService.class);
        ReportCardService second = context.getBean(ReportCardService.class);
        assertThat(first).isSameAs(second);   // Topic 2: singleton scope, proven by a test
    }
}
