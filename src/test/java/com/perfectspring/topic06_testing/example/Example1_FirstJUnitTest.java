package com.perfectspring.topic06_testing.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * EXAMPLE 1 — YOUR FIRST JUnit TESTS (no Spring, no mocks)
 *
 * Each test: ARRANGE -> ACT -> ASSERT. One behaviour per test, with a descriptive name.
 */
class Example1_FirstJUnitTest {

    private ScoreCalculator calculator;

    @BeforeEach                       // runs before EVERY test: each test gets a fresh calculator
    void setUp() {
        calculator = new ScoreCalculator();
    }

    @Test
    @DisplayName("Average of 80, 90 and 100 is 90")
    void averageOfThreeScores() {
        // ARRANGE
        List<Integer> scores = List.of(80, 90, 100);

        // ACT
        double average = calculator.average(scores);

        // ASSERT
        assertEquals(90.0, average);
    }

    @Test
    void scoreOf70IsGradeA() {
        assertThat(calculator.grade(70)).isEqualTo("A");       // AssertJ style: reads like English
    }

    @Test
    void scoreOf39IsAFail() {
        assertThat(calculator.grade(39)).isEqualTo("F");
    }

    @Test
    void averageOfEmptyListIsNotAllowed() {
        // assertThrows: "this code MUST throw this exception"
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> calculator.average(List.of()));
        assertThat(error.getMessage()).contains("At least one score");
    }

    @Test
    void scoreAbove100IsRejected() {
        assertThrows(IllegalArgumentException.class, () -> calculator.grade(101));
    }

    /** One test, many inputs. Each CSV line becomes its own test run. Great for edge cases! */
    @ParameterizedTest(name = "score {0} -> grade {1}")
    @CsvSource({
            "100, A", "70, A",
            "69, B", "60, B",
            "59, C", "50, C",
            "49, D", "45, D",
            "44, E", "40, E",
            "39, F", "0, F"
    })
    void gradeBoundaries(double score, String expectedGrade) {
        assertThat(calculator.grade(score)).isEqualTo(expectedGrade);
    }
}
