package com.perfectspring.topic06_testing.example;

import java.util.List;

/** "Somewhere scores come from": a database, a file, a web service... In tests, a MOCK. */
public interface ScoreSource {

    List<Integer> scoresFor(String student);
}
