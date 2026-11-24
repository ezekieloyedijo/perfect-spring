package com.perfectspring.topic03_dataaccess.example;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.util.List;

/**
 * EXAMPLE 2 — THE SAME WORK WITH JdbcTemplate
 *
 * Story: we hire a RECORDS CLERK (JdbcTemplate). We only hand over the SQL slip.
 * The clerk handles keys, drawers, loops, errors and closing up.
 *
 * Also shows:
 *   - DataSource   (the key desk)
 *   - RowMapper    (the form template: row -> object)
 *   - Exception translation (DuplicateKeyException instead of a cryptic SQLException)
 */
public class Example2_JdbcTemplate {

    /** A simple Java object for one row. A `record` is a short way to write a data class. */
    record Student(int id, String name, int score) {
    }

    @Configuration
    static class DbConfig {

        @Bean
        DataSource dataSource() {                       // the "key desk": knows how to reach the DB
            return new EmbeddedDatabaseBuilder()
                    .generateUniqueName(true)
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource) {   // the "records clerk"
            return new JdbcTemplate(dataSource);
        }
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(DbConfig.class)) {
            JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);

            // Every line below is REAL WORK. No ritual!
            jdbc.execute("CREATE TABLE student (id INT PRIMARY KEY, name VARCHAR(50), score INT)");
            jdbc.update("INSERT INTO student (id, name, score) VALUES (?, ?, ?)", 1, "Ada", 88);
            jdbc.update("INSERT INTO student (id, name, score) VALUES (?, ?, ?)", 2, "Tunde", 72);

            // RowMapper: "for each row, build a Student like this"
            RowMapper<Student> studentMapper = (row, rowNumber) ->
                    new Student(row.getInt("id"), row.getString("name"), row.getInt("score"));

            List<Student> students = jdbc.query("SELECT * FROM student", studentMapper);
            System.out.println("Students in the database (JdbcTemplate):");
            students.forEach(s -> System.out.println("  " + s));

            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM student", Integer.class);
            System.out.println("Total students: " + count);

            int changed = jdbc.update("UPDATE student SET score = ? WHERE id = ?", 95, 2);
            System.out.println("Rows updated: " + changed);

            Student tunde = jdbc.queryForObject("SELECT * FROM student WHERE id = ?", studentMapper, 2);
            System.out.println("Tunde after update: " + tunde);

            // Exception translation: insert id 1 AGAIN (a duplicate)
            try {
                jdbc.update("INSERT INTO student (id, name, score) VALUES (?, ?, ?)", 1, "Copycat", 0);
            } catch (DuplicateKeyException e) {           // a clear, specific Spring exception
                System.out.println();
                System.out.println("Spring translated the database error into: " + e.getClass().getSimpleName());
                System.out.println("It is a DataAccessException? " + (e instanceof DataAccessException));
            }
        }
    }
}
