package com.perfectspring.topic06_testing.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EXAMPLE 2 — MOCKS WITH MOCKITO
 *
 * ReportCardService needs a ScoreSource (in real life: a database).
 * We don't want a database in a unit test, so we use a STAND-IN (mock) and script its answers.
 */
@ExtendWith(MockitoExtension.class)           // switch Mockito on for this test class
class Example2_MockitoTest {

    @Mock
    ScoreSource scoreSource;                  // the stand-in actor

    @InjectMocks
    ReportCardService service;                // the REAL class, with the mock plugged into its constructor

    @Test
    void buildsReportFromTheScoresItIsGiven() {
        // ARRANGE: script the stand-in
        when(scoreSource.scoresFor("ada")).thenReturn(List.of(70, 80, 90));

        // ACT
        String report = service.reportFor("ada");

        // ASSERT: the result...
        assertThat(report).isEqualTo("ada: average 80.0, grade A");
        // ...and the interaction: "did the service really ask the score source?"
        verify(scoreSource).scoresFor("ada");
    }

    @Test
    void saysNoScoresYetWhenListIsEmpty() {
        when(scoreSource.scoresFor("newbie")).thenReturn(List.of());

        assertThat(service.reportFor("newbie")).isEqualTo("newbie: no scores yet");
    }

    /** The SAME idea WITHOUT annotations: this is why constructor injection matters (Topic 2!). */
    @Test
    void createMockByHandAndPassItToTheConstructor() {
        ScoreSource fakeSource = mock(ScoreSource.class);
        when(fakeSource.scoresFor("tunde")).thenReturn(List.of(30, 40));

        ReportCardService serviceByHand = new ReportCardService(fakeSource);   // plain `new`, no Spring

        assertThat(serviceByHand.reportFor("tunde")).contains("grade F");
    }
}
