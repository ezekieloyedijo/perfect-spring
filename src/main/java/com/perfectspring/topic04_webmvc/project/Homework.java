package com.perfectspring.topic04_webmvc.project;

/**
 * One homework item.
 *
 * Jackson (the JSON translator) needs:
 *   - an EMPTY constructor   -> to create the object when reading JSON
 *   - getters                -> to write JSON
 *   - setters                -> to fill the object when reading JSON
 */
public class Homework {

    private Long id;
    private String subject;
    private String description;
    private boolean done;

    public Homework() {
    }

    public Homework(Long id, String subject, String description) {
        this.id = id;
        this.subject = subject;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}
