package com.perfectspring.topic05_security.example;

import java.util.List;
import java.util.Map;

/**
 * EXAMPLE 1 — THE FILTER CHAIN IDEA (plain Java, no Spring)
 *
 * This is NOT how Spring Security is written internally. It is a tiny MODEL
 * so you can SEE the idea: a request must pass every checkpoint, in order,
 * BEFORE it reaches the office (your controller).
 *
 *   Checkpoint 1: Authentication  -> "Who are you?"        fail = 401
 *   Checkpoint 2: Authorization   -> "Are you allowed?"    fail = 403
 *   Then:         the controller does the real work        200
 */
public class Example1_FilterChainIdea {

    /** A visitor arriving at the gate. */
    record Request(String path, String username, String password) {
    }

    /** The school register: username -> {password, role}. (Real apps store HASHES, see Example 2.) */
    static final Map<String, String[]> REGISTER = Map.of(
            "ada", new String[]{"ada123", "STUDENT"},
            "admin", new String[]{"admin123", "ADMIN"});

    /** A checkpoint returns null to say "pass", or an error message to STOP the request. */
    interface Checkpoint {
        String check(Request request);
    }

    static final Checkpoint AUTHENTICATION = request -> {
        if (request.path().startsWith("/public")) return null;                 // public: no ID needed
        if (request.username() == null) {
            return "401 Unauthorized - who are you? (no ID card shown)";
        }
        String[] record = REGISTER.get(request.username());
        if (record == null || !record[0].equals(request.password())) {
            return "401 Unauthorized - who are you?";
        }
        return null;
    };

    static final Checkpoint AUTHORIZATION = request -> {
        if (request.path().startsWith("/admin")) {
            String role = REGISTER.get(request.username())[1];
            if (!role.equals("ADMIN")) return "403 Forbidden - your ID card colour is wrong for this room";
        }
        return null;
    };

    static final List<Checkpoint> FILTER_CHAIN = List.of(AUTHENTICATION, AUTHORIZATION);

    static String handle(Request request) {
        for (Checkpoint checkpoint : FILTER_CHAIN) {
            String problem = checkpoint.check(request);
            if (problem != null) {
                return problem;                    // stopped at the gate: the controller NEVER runs
            }
        }
        return "200 OK - welcome to " + request.path();   // reached the "controller"
    }

    public static void main(String[] args) {
        List<Request> visitors = List.of(
                new Request("/public/notice-board", null, null),
                new Request("/results/my", null, null),
                new Request("/results/my", "ada", "wrong-password"),
                new Request("/results/my", "ada", "ada123"),
                new Request("/admin/users", "ada", "ada123"),
                new Request("/admin/users", "admin", "admin123"));

        for (Request visitor : visitors) {
            System.out.printf("%-22s user=%-6s -> %s%n", visitor.path(), visitor.username(), handle(visitor));
        }
    }
}
