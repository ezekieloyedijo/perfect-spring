package com.perfectspring.topic03_dataaccess.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * EXAMPLE 1 — PLAIN JDBC (no Spring): feel the pain
 *
 * Story: you do EVERYTHING yourself in the records room:
 * unlock, open drawers, copy each file by hand, close drawers, lock up.
 *
 * Count the lines marked "RITUAL" vs the lines marked "REAL WORK".
 */
public class Example1_PlainJdbc {

    // An in-memory H2 database. DB_CLOSE_DELAY=-1 keeps it alive until the program ends.
    private static final String URL = "jdbc:h2:mem:plainjdbc;DB_CLOSE_DELAY=-1";

    public static void main(String[] args) {

        // RITUAL: get a connection ("unlock the records room").
        //         try-with-resources closes it for us at the end. Old code had to do it in `finally`.
        try (Connection connection = DriverManager.getConnection(URL, "sa", "")) {

            // REAL WORK: create a table
            try (Statement statement = connection.createStatement()) {                 // RITUAL
                statement.execute("CREATE TABLE student (id INT PRIMARY KEY, name VARCHAR(50), score INT)");
            }

            // REAL WORK: insert two rows
            try (PreparedStatement insert = connection.prepareStatement(              // RITUAL
                    "INSERT INTO student (id, name, score) VALUES (?, ?, ?)")) {
                insert.setInt(1, 1);                                                    // RITUAL
                insert.setString(2, "Ada");                                             // RITUAL
                insert.setInt(3, 88);                                                   // RITUAL
                insert.executeUpdate();

                insert.setInt(1, 2);                                                    // RITUAL
                insert.setString(2, "Tunde");                                           // RITUAL
                insert.setInt(3, 72);                                                   // RITUAL
                insert.executeUpdate();
            }

            // REAL WORK: read the rows back
            try (PreparedStatement select = connection.prepareStatement("SELECT * FROM student");   // RITUAL
                 ResultSet rows = select.executeQuery()) {                                         // RITUAL
                System.out.println("Students in the database (plain JDBC):");
                while (rows.next()) {                                                   // RITUAL: loop by hand
                    int id = rows.getInt("id");                                         // RITUAL: copy by hand
                    String name = rows.getString("name");                               // RITUAL
                    int score = rows.getInt("score");                                   // RITUAL
                    System.out.println("  " + id + " | " + name + " | " + score);
                }
            }

        } catch (SQLException e) {          // RITUAL: a CHECKED exception we are forced to catch
            System.out.println("Something went wrong: " + e.getMessage());
        }

        System.out.println();
        System.out.println("That was a lot of ritual for 3 small SQL statements. See Example 2 for the Spring way.");
    }
}
