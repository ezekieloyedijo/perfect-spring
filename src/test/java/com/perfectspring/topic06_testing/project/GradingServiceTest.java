package com.perfectspring.topic06_testing.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * UNIT TEST (bottom of the pyramid): GradingService alone, repository mocked.
 * No Spring is started, so it runs in milliseconds.
 */
@ExtendWith(MockitoExtension.class)
class GradingServiceTest {

    @Mock
    ScoreRepository repository;

    @InjectMocks
    GradingService service;

    @Test
    void averageOfSeventyIsGradeA() {
        when(repository.findById(1L)).thenReturn(Optional.of(new StudentScores(1L, "Ada", List.of(60, 80))));

        ResultCard card = service.resultFor(1L);

        assertThat(card.average()).isEqualTo(70.0);
        assertThat(card.grade()).isEqualTo("A");
        assertThat(card.passed()).isTrue();
    }

    @Test
    void averageOfFortyIsAPass() {
        when(repository.findById(2L)).thenReturn(Optional.of(new StudentScores(2L, "Tunde", List.of(40, 40))));

        ResultCard card = service.resultFor(2L);

        assertThat(card.grade()).isEqualTo("E");
        assertThat(card.passed()).isTrue();
    }

    @Test
    void unknownStudentThrowsNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resultFor(999L))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessage("Student #999 not found");
    }

    @Test
    void gradeBoundariesAreCorrect() {          // no mock needed: pure logic
        assertThat(service.gradeFor(69.9)).isEqualTo("B");
        assertThat(service.gradeFor(50)).isEqualTo("C");
        assertThat(service.gradeFor(39.9)).isEqualTo("F");
    }
}
