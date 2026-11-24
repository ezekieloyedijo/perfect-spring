package com.perfectspring.topic03_dataaccess.project;

/** One row of the `student` table, as a Java object. */
public class Student {

    private final Long id;
    private final String name;
    private final String className;
    private final int score;

    public Student(Long id, String name, String className, int score) {
        this.id = id;
        this.name = name;
        this.className = className;
        this.score = score;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getClassName() { return className; }
    public int getScore() { return score; }

    @Override
    public String toString() {
        return String.format("#%d %-8s %-5s score=%d", id, name, className, score);
    }
}
