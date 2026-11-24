# Lesson 3 — Spring Data Access

**Goal of this lesson:** save and read data in a real database without drowning in boilerplate, and keep data safe when things go wrong.

---

## The big picture story: "The School Records Room"

| Real life | Data access |
|---|---|
| A **whiteboard** (wiped every evening) | Java objects in memory |
| The **records room** with filing cabinets (permanent) | A **database** |
| A **register sheet** with columns: ID, Name, Class, Score | A **table** |
| One **line** on the sheet | A **row** |
| The **language** you use to ask the records officer for something | **SQL** |
| Doing everything yourself: unlock, open, copy, close, lock | **Plain JDBC** |
| The **key desk** at the entrance that lends out keys and takes them back | **`DataSource`** (connection pool) |
| A trained **records clerk** who handles all the doors and drawers for you | **`JdbcTemplate`** |
| A **form template** used to copy a line from the register onto a Student ID card | **`RowMapper`** |
| A **translator** who turns every cabinet's strange error codes into plain English | **`DataAccessException`** |
| **Paying school fees**: money leaves the parent AND reaches the school, or neither happens | **Transaction** (`@Transactional`) |

---

## Concept 1 — Why a database?

**Plain meaning:** Java objects live in **memory (RAM)**, which is wiped when the program stops. A **database** stores data on disk, so it is still there tomorrow.

**Real-life illustration:** a teacher writes scores on the **whiteboard**. The cleaner wipes it at night and the scores are gone. So the school keeps a **records room** with filing cabinets. That's the database.

**Hold on to:** *memory = whiteboard, database = filing cabinet.*

---

## Concept 2 — Tables, rows, columns and SQL

**Plain meaning:** a relational database stores data in **tables**. A table has **columns** (the kinds of information) and **rows** (one entry each).

| id | name | class_name | score |
|---|---|---|---|
| 1 | Ada | JSS1 | 88 |
| 2 | Tunde | JSS1 | 72 |

**SQL** is the language for talking to the table. You only need **four verbs**, often called **CRUD**:

| CRUD | SQL | Meaning |
|---|---|---|
| **C**reate | `INSERT INTO student (name, class_name, score) VALUES ('Ada','JSS1',88)` | add a row |
| **R**ead | `SELECT * FROM student WHERE class_name = 'JSS1'` | get rows |
| **U**pdate | `UPDATE student SET score = 90 WHERE id = 1` | change a row |
| **D**elete | `DELETE FROM student WHERE id = 1` | remove a row |

**Hold on to:** a table is just a **register sheet**, and SQL is how you **ask the records officer** for things.

---

## Concept 3 — Plain JDBC (feel the pain)

**Plain meaning:** **JDBC** (Java DataBase Connectivity) is Java's built-in toolkit for sending SQL to a database. The main pieces:

| JDBC piece | Records room story |
|---|---|
| `Connection` | Your **key** to the records room |
| `PreparedStatement` | The **request slip** with your SQL written on it |
| `ResultSet` | The **pile of files** that comes back, read one at a time |
| `close()` | **Returning the key**. You MUST do it. |

**Real-life illustration:** every single time you need one file, you queue at the key desk, unlock the room, find the cabinet, open the drawer, copy the file by hand, close the drawer, lock the room and return the key. **Every. Single. Time.**

👉 Run `example/Example1_PlainJdbc.java` and count how much code is "ritual" vs real work.

**Hold on to:** JDBC works, but **most of the code is ritual**, and forgetting `close()` causes leaks.

---

## Concept 4 — `DataSource`: where connections come from

**Plain meaning:** a **`DataSource`** is an object that knows *how to reach the database* (URL, username, password) and **hands out connections**. Good ones keep a **pool** of ready connections and reuse them.

**Real-life illustration:** the **key desk** at the entrance of the records room. You don't cut a new key each visit. You **borrow one and give it back**, so the next person can use it.

```java
@Bean
public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()          // creates an in-memory H2 database
            .setType(EmbeddedDatabaseType.H2)
            .addScript("topic03/schema.sql")       // create the tables
            .addScript("topic03/data.sql")         // add starting rows
            .build();
}
```

**Hold on to:** you configure the `DataSource` **once**. Everything else (JdbcTemplate, transactions) uses it.

---

## Concept 5 — `JdbcTemplate`: Spring does the ritual

**Plain meaning:** **`JdbcTemplate`** is a Spring class that does *all* the JDBC ritual for you: get connection, prepare statement, run it, loop over results, handle errors, close everything. **You only give it the SQL and the values.**

**Real-life illustration:** a trained **records clerk**. You say *"Please bring me every JSS1 student."* The clerk handles keys, cabinets and drawers, and hands you neat cards.

| You want to... | JdbcTemplate method |
|---|---|
| INSERT / UPDATE / DELETE | `jdbc.update(sql, values...)` → returns the number of rows changed |
| Read many rows | `jdbc.query(sql, rowMapper, values...)` → returns a `List` |
| Read exactly one value or row | `jdbc.queryForObject(sql, Type.class or rowMapper, values...)` |

```java
jdbc.update("INSERT INTO student (name, class_name, score) VALUES (?, ?, ?)", "Ada", "JSS1", 88);
Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM student", Integer.class);
```

> 🔐 **Always use `?` placeholders**, never glue values into the SQL string with `+`. The `?` protects you from **SQL injection** (a hacker typing SQL into your form).

**Hold on to:** **20 lines of JDBC become 1 line.** That's the whole reason JdbcTemplate exists.

---

## Concept 6 — `RowMapper`: turning a row into an object

**Plain meaning:** the database returns **rows**. Your Java code wants **objects** (`Student`). A **`RowMapper`** is a small piece of code that says *"for each row, build an object like this."* JdbcTemplate calls it once for every row.

**Real-life illustration:** a **form template**. The clerk copies each line of the register onto a blank **Student ID card**: "Name goes here, Class goes here, Score goes here."

```java
private final RowMapper<Student> studentMapper = (row, rowNumber) -> new Student(
        row.getLong("id"),
        row.getString("name"),
        row.getString("class_name"),
        row.getInt("score"));

List<Student> all = jdbc.query("SELECT * FROM student", studentMapper);
```

**Hold on to:** write the RowMapper **once**, then reuse it in every query that returns students.

---

## Concept 7 — Exception translation (`DataAccessException`)

**Plain meaning:** when something goes wrong, every database speaks its own error language (MySQL error 1062, Oracle ORA-00001, ...). Spring **translates** them all into one family of clear, **unchecked** exceptions, all children of **`DataAccessException`**.

| Problem | Spring exception |
|---|---|
| Same ID inserted twice | `DuplicateKeyException` |
| Expected one row, found none | `EmptyResultDataAccessException` |
| Broke a rule like NOT NULL | `DataIntegrityViolationException` |
| Bad SQL | `BadSqlGrammarException` |

**Real-life illustration:** a **translator** at the records room. Whether the error came from the Chinese cabinet or the French cabinet, you hear: *"That student ID already exists."*

**Hold on to:** you're **not forced** to catch them (they're unchecked), and if you switch databases your error handling still works. Putting `@Repository` on your class also tells Spring "translate errors coming out of here".

---

## Concept 8 — Transactions: all-or-nothing (`@Transactional`)

**Plain meaning:** a **transaction** groups several database changes into **one unit**. Either **all** of them succeed (**commit**), or if anything fails, **all** are undone (**rollback**).

**Real-life illustration:** paying **school fees by bank transfer**:
1. Take ₦50,000 from the parent's account.
2. Add ₦50,000 to the school's account.

If the power goes off after step 1, the money must **not vanish**. The bank undoes step 1. **All or nothing.**

```java
@Service
public class TransferService {

    @Transactional                // Spring: start a transaction before, commit after, roll back on error
    public void transfer(String from, String to, int amount) {
        jdbc.update("UPDATE account SET balance = balance - ? WHERE owner = ?", amount, from);
        if (somethingGoesWrong) throw new RuntimeException("Power cut!");
        jdbc.update("UPDATE account SET balance = balance + ? WHERE owner = ?", amount, to);
    }
}
```

To switch transactions on (without Spring Boot) you need two things in your config:
```java
@EnableTransactionManagement     // "please respect @Transactional"
...
@Bean
PlatformTransactionManager transactionManager(DataSource ds) {
    return new DataSourceTransactionManager(ds);   // the "bank manager" who commits / rolls back
}
```

**Two rules beginners must know:**
1. By default Spring rolls back on **unchecked** exceptions (`RuntimeException`), not on checked ones.
2. `@Transactional` works when the method is called **from another bean**. Calling it from inside the same class skips it (Spring wraps the bean in a "proxy" bodyguard, and internal calls don't pass through the bodyguard).

👉 Run `example/Example3_Transactions.java`. It does the transfer twice, once *without* and once *with* a transaction.

**Hold on to:** **`@Transactional` = all-or-nothing.** Put it on service methods that change several things together.

---

## Concept 9 — Preview: ORM, JPA and Spring Data

**Plain meaning:** writing SQL and RowMappers by hand is fine for small apps. An **ORM** (Object-Relational Mapper) such as **Hibernate** maps Java classes to tables **automatically**. **JPA** is the Java standard for ORMs. **Spring Data JPA** goes one step further: you write an **interface**, and Spring writes the code.

```java
@Entity class Course { @Id @GeneratedValue Long id; String title; }   // class <-> table
interface CourseRepository extends JpaRepository<Course, Long> { }    // CRUD methods for free!
```

**Real-life illustration:** instead of a clerk who needs your SQL slip, you get a **smart assistant**. You just say *"find courses by title"* and it writes the slip itself.

**Hold on to:** we use this for real in **Topic 7 (Spring Boot)**, because Boot sets it up in seconds.

---

## Putting it all together (tell it as one story)

> "Memory forgets, so I store data in a **database** made of **tables**, and I talk to it with **SQL**. Plain **JDBC** makes me repeat an open/close ritual everywhere. So I configure a **`DataSource`** once (the key desk) and use **`JdbcTemplate`** (the records clerk), which does the ritual for me. A **`RowMapper`** turns each row into an object. If something goes wrong, Spring gives me a clear **`DataAccessException`**. And when several changes must succeed together, **`@Transactional`** makes them **all-or-nothing**."

---

## The project: Student Records

| File | What it shows |
|---|---|
| `schema.sql`, `data.sql` | Table creation + starting data |
| `DatabaseConfig` | `DataSource`, `JdbcTemplate`, `TransactionManager`, `@EnableTransactionManagement` |
| `Student` | One row as a Java object |
| `StudentRepository` | Full CRUD with `JdbcTemplate` + a reusable `RowMapper` |
| `StudentService` | `@Transactional` batch of exam results (one bad score cancels the whole batch) |
| `StudentRecordsApp` | Runs everything and prints the results |

---

# ✅ Test Yourself

## Part A — Quick check

1. Why can't we just keep data in a Java `List` in a real app?
2. What do the letters CRUD stand for? Give the SQL keyword for each.
3. Name the three main JDBC objects and their records-room equivalents.
4. What happens if you forget to close JDBC connections?
5. What is a `DataSource`, and why does a connection *pool* help?
6. Which `JdbcTemplate` method would you use for (a) `DELETE`, (b) reading a list of students, (c) `SELECT COUNT(*)`?
7. What is a `RowMapper` for?
8. Why should you use `?` instead of building SQL with `+`?
9. What is the parent class of all Spring database exceptions? Are they checked or unchecked?
10. Define "commit" and "rollback" in one sentence each.
11. What two things must you add to a plain Spring config for `@Transactional` to work?
12. A `@Transactional` method throws a checked `Exception`. Does it roll back by default?

## Part B — Explain it like a teacher

13. Explain transactions using a real-life example that is **not** money (e.g. booking a flight + hotel, a football transfer).
14. Explain to a classmate why `JdbcTemplate` is like a records clerk.

## Part C — Hands-on tasks

15. In `StudentRepository`, add a method `findTopStudents(int minScore)` that returns students with a score ≥ `minScore`. Call it from `StudentRecordsApp`.
16. In `data.sql`, add two more students. Run the app. Do you see them?
17. In `StudentRecordsApp`, change the bad score (`150`) in the exam batch to `95`. Run again. What's different, and why?
18. Remove `@Transactional` from `StudentService.recordExamResults` and run with the bad score. What happens to the good scores in that batch? Explain.

---

<details>
<summary><b>👩‍🏫 Answer key (click to open)</b></summary>

1. The list lives in memory and is wiped when the app stops. It also can't be shared safely by many servers.
2. Create (`INSERT`), Read (`SELECT`), Update (`UPDATE`), Delete (`DELETE`).
3. `Connection` (key to the room), `PreparedStatement` (request slip with the SQL), `ResultSet` (pile of files returned).
4. The database runs out of connections ("connection leak"), and the app slows down or freezes.
5. An object that hands out database connections. A pool reuses ready connections instead of creating a new one each time, which is much faster.
6. (a) `update(...)` (b) `query(sql, rowMapper)` (c) `queryForObject(sql, Integer.class)`.
7. Converting each row of a result into a Java object.
8. To prevent SQL injection. `?` values are sent separately and treated as data, never as SQL.
9. `DataAccessException`. Unchecked (it extends `RuntimeException`).
10. **Commit**: make all the changes permanent. **Rollback**: undo every change made in the transaction.
11. `@EnableTransactionManagement` and a `PlatformTransactionManager` bean (e.g. `DataSourceTransactionManager`).
12. **No.** By default only unchecked exceptions (`RuntimeException`, `Error`) cause rollback. Use `@Transactional(rollbackFor = Exception.class)` to change that.
13. Booking a trip: reserve the flight AND the hotel. If the hotel is full, cancel the flight too. You never end up with a flight and no hotel.
14. You only say what you want (SQL). The clerk (`JdbcTemplate`) handles keys, drawers, closing and errors.
15. `return jdbc.query("SELECT * FROM student WHERE score >= ? ORDER BY score DESC", studentMapper, minScore);`
16. Yes. `data.sql` runs every time the in-memory database starts.
17. The whole batch is saved, because no exception was thrown, so the transaction commits.
18. The scores **before** the bad one are saved and stay saved (no rollback). The data is now half-updated. That's exactly the problem transactions solve.
</details>
