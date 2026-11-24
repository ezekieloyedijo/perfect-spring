package com.perfectspring.topic03_dataaccess.project;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * The RECORDS OFFICE: the only class that writes SQL.
 *
 * JdbcTemplate = the records clerk (does the open/close ritual)
 * RowMapper    = the form template (turns each row into a Student)
 * @Repository  = badge + "translate database errors into DataAccessException"
 */
@Repository
public class StudentRepository {

    private final JdbcTemplate jdbc;

    // Written ONCE, reused by every query that returns students.
    private final RowMapper<Student> studentMapper = (row, rowNumber) -> new Student(
            row.getLong("id"),
            row.getString("name"),
            row.getString("class_name"),
            row.getInt("score"));

    public StudentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------- C: CREATE ----------
    public void save(String name, String className, int score) {
        jdbc.update("INSERT INTO student (name, class_name, score) VALUES (?, ?, ?)", name, className, score);
    }

    // ---------- R: READ ----------
    public List<Student> findAll() {
        return jdbc.query("SELECT * FROM student ORDER BY id", studentMapper);
    }

    public List<Student> findByClass(String className) {
        return jdbc.query("SELECT * FROM student WHERE class_name = ? ORDER BY name", studentMapper, className);
    }

    public Optional<Student> findById(long id) {
        try {
            return Optional.of(jdbc.queryForObject("SELECT * FROM student WHERE id = ?", studentMapper, id));
        } catch (EmptyResultDataAccessException e) {   // Spring's clear "no row found" exception
            return Optional.empty();
        }
    }

    public int count() {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM student", Integer.class);
        return total == null ? 0 : total;
    }

    // ---------- U: UPDATE ----------
    public int updateScore(long id, int newScore) {
        return jdbc.update("UPDATE student SET score = ? WHERE id = ?", newScore, id);   // returns rows changed
    }

    // ---------- D: DELETE ----------
    public int deleteById(long id) {
        return jdbc.update("DELETE FROM student WHERE id = ?", id);
    }
}
