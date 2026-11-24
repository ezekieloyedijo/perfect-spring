package com.perfectspring.topic07_boot.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * @Entity = "this class IS a database table". Hibernate creates the table `course` for us.
 * Each field becomes a column. Watch the CREATE TABLE in the console at start-up!
 */
@Entity
public class Course {

    @Id                                                   // the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // the database numbers it: 1, 2, 3...
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false)
    private String title;

    private String teacher;

    private int capacity;

    private int enrolled;

    /** JPA needs an empty constructor to build objects from rows. */
    protected Course() {
    }

    public Course(String code, String title, String teacher, int capacity) {
        this.code = code;
        this.title = title;
        this.teacher = teacher;
        this.capacity = capacity;
    }

    public boolean isFull() {
        return enrolled >= capacity;
    }

    public void enrollOneStudent() {
        enrolled++;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getTeacher() { return teacher; }
    public int getCapacity() { return capacity; }
    public int getEnrolled() { return enrolled; }
    public int getSeatsLeft() { return capacity - enrolled; }     // appears in the JSON too
}
