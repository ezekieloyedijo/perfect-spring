package com.perfectspring.topic08_cloud.example;

/**
 * EXAMPLE 2 — HOW A CIRCUIT BREAKER THINKS (plain Java model, no libraries)
 *
 * Story: the electrical BREAKER in a house.
 *   CLOSED    -> power flows (calls go through)
 *   OPEN      -> tripped: don't even try, answer with the FALLBACK at once
 *   HALF_OPEN -> after a rest, carefully try ONE call. Success -> CLOSED, failure -> OPEN again
 *
 * The real thing (Resilience4j) works on the same idea, with time windows and failure percentages.
 */
public class Example2_CircuitBreakerIdea {

    enum State { CLOSED, OPEN, HALF_OPEN }

    static class SimpleCircuitBreaker {
        private static final int FAILURES_BEFORE_TRIPPING = 3;
        private static final int CALLS_TO_REST_WHILE_OPEN = 2;

        private State state = State.CLOSED;
        private int consecutiveFailures = 0;
        private int restCounter = 0;

        String call(boolean serviceIsHealthy) {
            if (state == State.OPEN) {
                restCounter++;
                if (restCounter > CALLS_TO_REST_WHILE_OPEN) {
                    state = State.HALF_OPEN;               // rested long enough: try one test call
                } else {
                    return "FALLBACK (breaker OPEN, didn't even try)";
                }
            }

            if (serviceIsHealthy) {
                boolean wasTestCall = state == State.HALF_OPEN;
                consecutiveFailures = 0;
                state = State.CLOSED;
                return wasTestCall ? "real answer (HALF-OPEN test call worked!)" : "real answer from the service";
            }

            if (state == State.HALF_OPEN) {
                state = State.OPEN;                        // the test call failed: trip again and rest again
                restCounter = 0;
                return "FALLBACK (HALF-OPEN test call failed)";
            }

            consecutiveFailures++;
            if (consecutiveFailures >= FAILURES_BEFORE_TRIPPING) {
                state = State.OPEN;                        // trip!
                restCounter = 0;
            }
            return "FALLBACK (the call failed)";
        }

        State state() {
            return state;
        }
    }

    public static void main(String[] args) {
        SimpleCircuitBreaker breaker = new SimpleCircuitBreaker();

        // true = the catalog service is healthy, false = it is down
        // Story: healthy -> goes down for a while (trips, rests, a test fails, rests again) -> comes back.
        boolean[] serviceHealth = {true, true, false, false, false, false, false, false, false, false, true, true};

        for (int i = 0; i < serviceHealth.length; i++) {
            String answer = breaker.call(serviceHealth[i]);
            System.out.printf("Call %2d | service %-4s | answer: %-44s | breaker now: %s%n",
                    i + 1, serviceHealth[i] ? "UP" : "DOWN", answer, breaker.state());
        }
    }
}
