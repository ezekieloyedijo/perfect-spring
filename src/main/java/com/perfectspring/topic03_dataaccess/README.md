# Topic 3 — Spring Data Access

> Read this file first. Then read [LESSON.md](LESSON.md). Then run `example/` in order, then `project/`.

---

## 1. The problem that existed

In Topic 2 our library kept books in a Java `List`. That is like writing on a **whiteboard**: the moment the app stops, **everything is wiped**. Real apps must remember data, so they use a **database** (a permanent notebook).

Java has a built-in way to talk to databases called **JDBC**. But plain JDBC is painful. To read **one** list of students you must:

```java
Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
try {
    conn = DriverManager.getConnection(url, user, pass);   // 1. unlock the records room
    ps = conn.prepareStatement("SELECT * FROM student");   // 2. find the right cabinet
    rs = ps.executeQuery();                                // 3. open the drawer
    while (rs.next()) { ... copy each column by hand ... } // 4. copy every file by hand
} catch (SQLException e) {                                 // 5. handle a confusing, checked error
    ...
} finally {
    rs.close(); ps.close(); conn.close();                  // 6. close drawer, cabinet, lock the room
}                                                          //    (forget this and the database slowly dies)
```

About **20 lines** of ritual for **one line** of real work (the SQL). Repeat that in every method of every class. The problems:

1. **Boilerplate everywhere.** The same open/close code copied hundreds of times.
2. **Leaks.** Forget one `close()` and the database runs out of connections. The app freezes.
3. **Ugly errors.** Every database (MySQL, Oracle, H2...) reports problems differently, through a checked `SQLException` you are forced to catch.
4. **Half-finished work.** If the app crashes halfway through *"take money from Ada, give money to the school"*, the money disappears.

**Spring Data Access** removes the ritual, translates the errors, and gives you **transactions** (all-or-nothing work).

---

## 2. The end goal of this topic

> **"I configure a `DataSource` once. I use `JdbcTemplate` to run SQL in one line, and Spring opens and closes everything for me. A `RowMapper` turns table rows into Java objects. Spring turns database errors into clear `DataAccessException`s. And `@Transactional` makes a group of changes all-or-nothing."**

You'll build a **Student Records** app that saves, reads, updates and deletes students in a real (in-memory) database, and safely records a batch of exam results.

---

## 3. The learning path (one step at a time)

| Step | Learn this | Which raises the question... |
|---|---|---|
| **1** | **Why a database?** Memory forgets. A database remembers. | "How is data organised inside a database?" |
| **2** | **Tables, rows, columns and SQL.** A table is like a register sheet. SQL is the language for asking it questions. | "How does Java send SQL to the database?" |
| **3** | **Plain JDBC.** Java's standard way to talk to databases, and why it's painful. | "Where do connections come from, and who manages them?" |
| **4** | **`DataSource`.** One place that hands out database connections. | "Can Spring do the open/close ritual for me?" |
| **5** | **`JdbcTemplate`.** You write the SQL. Spring does the ritual. | "How do I turn a row of data back into a Java object?" |
| **6** | **`RowMapper`.** A template that converts each row into an object. | "What happens when something goes wrong?" |
| **7** | **Exception translation (`DataAccessException`).** One clear family of errors, whatever the database. | "What if a group of changes fails halfway?" |
| **8** | **Transactions (`@Transactional`).** All-or-nothing work. | "Must I still write SQL forever?" |
| **9** | **Preview: ORM, JPA and Spring Data.** Map objects to tables automatically. (Used for real in Topic 7.) | Go to Topic 4. |

---

## 4. What is inside this folder

```
topic03_dataaccess/
├── README.md
├── LESSON.md
├── example/
│   ├── Example1_PlainJdbc.java       (Step 3: feel the pain)
│   ├── Example2_JdbcTemplate.java    (Steps 4-7: the same work, the Spring way)
│   └── Example3_Transactions.java    (Step 8: all-or-nothing money transfer)
└── project/  "Student Records"
    ├── Student.java             (plain object = one row)
    ├── StudentRepository.java   (@Repository using JdbcTemplate + RowMapper)
    ├── StudentService.java      (@Service with @Transactional)
    ├── DatabaseConfig.java      (DataSource, JdbcTemplate, TransactionManager)
    └── StudentRecordsApp.java   (main)

src/main/resources/topic03/
├── schema.sql   (creates the table)
└── data.sql     (puts starting rows in)
```

> 💡 We use **H2**, a tiny database that runs **inside memory**. Nothing to install. The same code works with MySQL or PostgreSQL. You only change the `DataSource` settings.

---

## 5. How to build the project, step by step

**The rule:** design the **table** first, then the **Java object** that matches one row, then the **repository** (the only class that writes SQL), then the **service** (business rules and transactions), then the **configuration** that connects everything to the database, and last the **launcher**.

### Step 1 — `src/main/resources/topic03/schema.sql` (the table)
**Why first:** the database shape decides everything else. Columns: `id` (numbered automatically), `name`, `class_name`, `score`.
- `CREATE TABLE student (...)`: builds the "register sheet".

### Step 2 — `src/main/resources/topic03/data.sql` (starting rows)
**Why now:** gives us something to read as soon as the app starts.
- `INSERT INTO student ...`: four starting students.

### Step 3 — `Student.java` (one row as a Java object)
**Why now:** the repository must turn rows into *something*. This is that something. One field per column.
- **Constructor** `Student(id, name, className, score)`: builds one student.
- `getId()`, `getName()`, `getClassName()`, `getScore()`: read the values.
- `toString()`: prints a neat line like `#1 Ada JSS1 score=88`.

### Step 4 — `StudentRepository.java` (`@Repository`: the only class that writes SQL)
**Why now:** it needs the table (Steps 1–2) and the `Student` class (Step 3).
- **Field** `studentMapper` (a `RowMapper`): the "form template" that turns **one row → one `Student`**. It's written once and reused by every query.
- **Constructor** `StudentRepository(JdbcTemplate)`: receives the "records clerk" (injection).
- `save(name, className, score)`: **C**reate. `INSERT` a new row.
- `findAll()`: **R**ead every student.
- `findByClass(className)`: **R**ead the students of one class.
- `findById(id)`: **R**ead one student. Returns empty if there's none (it catches Spring's `EmptyResultDataAccessException`).
- `count()`: how many students there are.
- `updateScore(id, newScore)`: **U**pdate. Returns how many rows changed.
- `deleteById(id)`: **D**elete. Returns how many rows were removed.

### Step 5 — `StudentService.java` (`@Service`: business rules + transactions)
**Why now:** it uses the repository, never SQL directly.
- **Constructor** `StudentService(StudentRepository)`: receives the repository.
- `recordExamResults(scoresById)` (`@Transactional`): updates a whole batch of scores **all or nothing**. If one score is invalid (below 0 or above 100) it throws an exception, and Spring **rolls back** every update in the batch.

### Step 6 — `DatabaseConfig.java` (`@Configuration`: connect to the database)
**Why now:** these are Spring/H2 library classes (we can't put badges on them), so we create them with `@Bean` methods.
- `@ComponentScan`: finds `StudentRepository` and `StudentService`.
- `@EnableTransactionManagement`: switches `@Transactional` on.
- `dataSource()` (`@Bean`): the "key desk". It creates the in-memory H2 database and runs `schema.sql` and `data.sql`.
- `jdbcTemplate(DataSource)` (`@Bean`): the "records clerk" that does the JDBC ritual for us.
- `transactionManager(DataSource)` (`@Bean`): the "bank manager" who commits or rolls back.

### Step 7 — `StudentRecordsApp.java` (the launcher)
**Why last:** it only starts things and shows the results.
- `main()`: starts the container, then demonstrates CRUD and finally the rolled-back batch.
- `printAll(title, repository)`: a small helper that prints the count and every student.

> 🔁 **The pattern to remember:** `schema.sql` → `data.sql` → row class → `@Repository` (`JdbcTemplate` + `RowMapper`) → `@Service` (`@Transactional`) → `@Configuration` (`DataSource`, `JdbcTemplate`, `TransactionManager`) → `main`.

---

## 6. How to run

```bash
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic03_dataaccess.example.Example1_PlainJdbc
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic03_dataaccess.example.Example2_JdbcTemplate
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic03_dataaccess.example.Example3_Transactions
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic03_dataaccess.project.StudentRecordsApp
```
