# Lesson 7 — Spring Boot

**Goal of this lesson:** understand what Spring Boot does for you automatically, how to control it, and how to build a real database-backed API quickly.

---

## The big picture story: "The Furnished School Building"

In Topics 1–4 you built the school **from bare land**: you poured the foundation, wired the electricity, installed the doors and hired the receptionist yourself.
Spring Boot hands you a **ready, furnished building** with a smart caretaker.

| Real life | Spring Boot |
|---|---|
| Bare land: you build and wire everything yourself | Plain Spring (Topics 1–4) |
| A **furnished building**, ready on day one | A Spring Boot app |
| **Kit boxes**: "Science Lab kit" contains matching microscopes, slides and gloves | **Starters** (`spring-boot-starter-web`, `-data-jpa`...) |
| A **smart caretaker** who sees what you brought in and sets it up | **Auto-configuration** |
| "You brought your own projector? Fine, I won't install mine." | Auto-config **backs off** when you define your own bean |
| The building has its **own generator** | The **embedded server** (Tomcat inside your app) |
| The **control panel** on the wall: temperature, lights, bells | **`application.properties`** |
| **Term mode / holiday mode** switch on the panel | **Profiles** (`dev`, `prod`...) |
| A **smart records assistant**: "find courses by title" and it just does it | **Spring Data JPA** repositories |
| The **form checker** at the door: "your name is empty, go back" | **Validation** (`@Valid`, `@NotBlank`) |
| **Opening-day tasks**: stock the shelves after the doors open | **`CommandLineRunner`** |
| The building's **health dashboard**: power OK, water OK | **Actuator** (`/actuator/health`) |
| A **portable cabin**: load it on a truck, set it down anywhere | The **runnable fat jar** (`java -jar`) |

---

## Concept 1 — The set-up pain (quick recap)

In Topic 4 we created Tomcat, the web container and the `DispatcherServlet` **by hand**. In Topic 3 we created the `DataSource`, `JdbcTemplate` and `TransactionManager` **by hand**. Almost **every** Spring app needs those same pieces, configured the same way.

**Hold on to:** Boot doesn't do anything magical. It **writes that standard set-up for you**, the same set-up you wrote yourself.

---

## Concept 2 — `@SpringBootApplication` and `SpringApplication.run`

**Plain meaning:** one annotation that combines three you already know (or will in a moment):

```java
@SpringBootApplication     // =  @Configuration          (this class can hold @Bean methods)
                           //  + @ComponentScan          (scan THIS package and below: Topic 2!)
                           //  + @EnableAutoConfiguration (the smart caretaker: Concept 4)
public class CourseRegistrationApp {
    public static void main(String[] args) {
        SpringApplication.run(CourseRegistrationApp.class, args);   // start EVERYTHING
    }
}
```

`SpringApplication.run(...)` creates the container (Topic 1), scans for beans (Topic 2), runs auto-configuration, starts Tomcat, and prints `Started ... in 2.3 seconds`.

**Real-life illustration:** instead of building the school, you **turn the key in the front door** of a ready building, and the lights, bells and reception all switch on.

**Hold on to:** because of `@ComponentScan`, **put the main class in the top package** of your app. Everything must be *below* it.

👉 Run `example/ex1_hello/HelloBootApp.java`, a full web app in about 20 lines.

---

## Concept 3 — Starters

**Plain meaning:** a **starter** is one dependency that pulls in a whole group of libraries that work together, at **versions tested together** (fixed by the `spring-boot-starter-parent` in our `pom.xml`).

| Starter | Brings you |
|---|---|
| `spring-boot-starter-web` | Spring MVC + embedded Tomcat + Jackson (JSON) |
| `spring-boot-starter-data-jpa` | Spring Data JPA + Hibernate + JDBC + transactions |
| `spring-boot-starter-validation` | Bean Validation (`@NotBlank`, `@Min`...) |
| `spring-boot-starter-security` | Spring Security (Topic 5) |
| `spring-boot-starter-test` | JUnit 5 + Mockito + AssertJ + Spring Test (Topic 6) |

**Real-life illustration:** a **"Science Lab kit box"**. You don't buy a microscope from one shop, slides from another and gloves from a third and hope they fit. One box, everything matches.

**Hold on to:** **no more version hell.** You choose *what* you need, and Boot chooses compatible *versions*.

---

## Concept 4 — Auto-configuration (the most important idea in Boot)

**Plain meaning:** at start-up, Boot runs a long list of **conditional configurations**. Each one says *"IF this condition is true, THEN create these beans"*:

| Condition Boot checks | So Boot creates... |
|---|---|
| Is Tomcat + Spring MVC on the classpath? | Tomcat, `DispatcherServlet`, JSON converters |
| Is H2 on the classpath, and **no** `DataSource` bean defined? | An in-memory H2 `DataSource` |
| Is there a `DataSource`? | `JdbcTemplate`, a `TransactionManager` |
| Is Hibernate there? | An `EntityManagerFactory`, JPA repositories |
| Is Spring Security there, and **no** `SecurityFilterChain` defined? | Lock everything + a default user (Topic 5) |

The key words are **"and no bean defined"**: auto-configuration **backs off** the moment you define your own bean. *You are always in control.*

**Real-life illustration:** a **smart caretaker**. *"I see you brought in a TV (H2 on the classpath), so I'll mount it and plug it in (a `DataSource`). Oh, you brought your own TV stand (your own `DataSource` bean)? Then I won't install mine."*

You can also **switch an auto-configuration off**:
```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```
That's exactly what this course does in `application.properties` for every topic except Topic 5!

👉 Run `example/ex2_autoconfig/AutoConfigPeekApp.java`. It lists beans like `DataSource`, `JdbcTemplate` and `ObjectMapper` that **you never wrote**.
👉 With the project running, open http://localhost:8087/actuator/conditions. It's Boot's own report of **which conditions matched and which didn't**.

**Hold on to:** **auto-configuration = "sensible defaults, based on what's on the classpath, that step aside when you provide your own."**

---

## Concept 5 — The embedded server

**Plain meaning:** the old way was to install Tomcat separately and **deploy a `.war` file into it**. Boot **puts Tomcat inside your app**. Your app *is* the server.

**Real-life illustration:** the building has its **own generator**. It doesn't need to be connected to the city's power station to switch on.

```properties
server.port=8087          # change the port: that's it
```

**Hold on to:** **your app starts its own web server.** That's what made Topic 4's `TomcatLauncher` unnecessary.

---

## Concept 6 — `application.properties`, profiles and `@ConfigurationProperties`

**Plain meaning:** `src/main/resources/application.properties` is Boot's **control panel**. Hundreds of settings are available:

```properties
server.port=8087
spring.datasource.url=jdbc:h2:mem:courses
spring.jpa.show-sql=true
school.name=Unity High School       # your OWN settings work too
```

**Profiles** = different panel settings for different situations. `application-dev.properties` and `application-prod.properties` hold the differences, and you switch them on with `--spring.profiles.active=prod` (or `.profiles("boot")` in code, like our apps do). Profile settings **override** the base file.

**`@ConfigurationProperties`** reads a whole group of your settings into **one typed object**, instead of many `@Value`s (Topic 2):

```java
@ConfigurationProperties(prefix = "school")
public record SchoolProperties(String name, int defaultCourseCapacity) { }
// school.name=...                  -> name
// school.default-course-capacity=  -> defaultCourseCapacity   (dashes become camelCase)
```

**Real-life illustration:** the **control panel on the wall**, with a **term / holiday mode switch** (profiles), and a **printed settings card** (`@ConfigurationProperties`) instead of scraps of paper (`@Value` everywhere).

> 🧭 In **this** course project, each topic's Boot app switches on its own profile (`security`, `testing`, `boot`, ...). So `application-<profile>.properties` holds that app's own settings, and `application.properties` holds what they share.

**Hold on to:** **change behaviour with settings, not code.** And never commit real production passwords into the file. Production passwords usually come from environment variables.

---

## Concept 7 — Spring Data JPA

**Plain meaning:** remember Topic 3's preview? Now it's real, and Boot sets it all up:

1. **`@Entity`** on a class = "this class is a database table". Hibernate (the ORM) creates the table for you.
2. **`interface CourseRepository extends JpaRepository<Course, Long>`** = Spring **writes the implementation** at start-up. You get `save`, `findAll`, `findById`, `deleteById`, `count`... with **zero code**.
3. **Derived queries:** declare a method **name** and Spring turns it into SQL:

```java
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTitleContainingIgnoreCase(String text); // SELECT ... WHERE UPPER(title) LIKE UPPER('%text%')
    boolean existsByCode(String code);                          // SELECT COUNT(*) > 0 ... WHERE code = ?
}
```

```java
@Entity
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;          // primary key, numbered by the database
    private String code;
    private String title;
    ...
}
```

**Bonus: dirty checking.** Inside a `@Transactional` method, if you change a field of an entity you loaded (`course.setEnrolled(...)`), JPA **notices and saves it automatically** at the end. No `UPDATE` needed.

**Real-life illustration:** the **smart records assistant**. Topic 3's clerk needed a written SQL slip. This assistant understands *"find courses whose title contains 'java'"* and writes the slip itself.

**Hold on to:** **Entity = table. Repository interface = free CRUD. Method name = query.** Watch the SQL it writes in the console (`spring.jpa.show-sql=true`).

---

## Concept 8 — Validation

**Plain meaning:** never trust incoming data. Put **rules as annotations** on your request class, and add **`@Valid`** in the controller. Bad data is rejected with **400** *before* your service runs.

```java
public record CourseRequest(
        @NotBlank @Size(max = 10) String code,
        @NotBlank String title,
        @Min(1) @Max(200) Integer capacity) { }

@PostMapping
public ResponseEntity<Course> create(@Valid @RequestBody CourseRequest request) { ... }
```

| Annotation | Rule |
|---|---|
| `@NotNull` | must be present |
| `@NotBlank` | text must not be empty or spaces |
| `@Size(min, max)` | length limits |
| `@Min` / `@Max` | number limits |
| `@Email` | must look like an email |
| `@Pattern(regexp = ...)` | must match a pattern |

**Real-life illustration:** the **form checker at the door**. "Your name box is empty and you wrote 500 in the 'number of students' box. Fix it before you go in."

**Hold on to:** **validation at the edge, business rules in the service.**

---

## Concept 9 — `CommandLineRunner`: code that runs at start-up

**Plain meaning:** a bean that implements **`CommandLineRunner`** has a `run(...)` method that Boot calls **once, right after the app has started**. It's perfect for loading starting data or printing a welcome message.

**Real-life illustration:** **opening-day tasks**. After the building opens, the caretaker stocks the shelves and puts up the first notices.

**Hold on to:** it runs **after** everything is wired, so you can safely use repositories inside it.

---

## Concept 10 — Actuator: the health dashboard

**Plain meaning:** **Spring Boot Actuator** adds ready-made **monitoring endpoints**:

| Endpoint | Shows |
|---|---|
| `/actuator/health` | `{"status":"UP"}`: is the app (and its database) OK? |
| `/actuator/info` | Information about the app |
| `/actuator/beans` | Every bean in the container |
| `/actuator/conditions` | The auto-configuration report |

Which ones are visible is controlled by `management.endpoints.web.exposure.include=...`.

**Real-life illustration:** the building's **health dashboard**: power OK, water OK, alarms OK. Cloud platforms (Topic 8) check `/actuator/health` to decide if your app needs restarting.

**Hold on to:** **production apps must be observable.** Actuator gives you that for free.

---

## Concept 11 — Packaging: the runnable "fat jar"

**Plain meaning:** `mvnw package` creates **one `.jar` file** containing your code, **all** libraries and the embedded Tomcat. Run it anywhere Java is installed:

```bash
mvnw clean package -DskipTests
java -jar target/perfect-spring-1.0.0.jar
java -jar target/perfect-spring-1.0.0.jar --server.port=9000    # override any setting from the command line
```

**Real-life illustration:** a **portable cabin classroom**: load it on a truck, set it down on any field, plug in, and it's ready. Docker containers and cloud platforms (Topic 8) love this.

**Hold on to:** **one file = the whole app.** No separate server installation.

---

## Putting it all together (tell it as one story)

> "Spring Boot is Spring with automatic set-up. I add **starters** to get matching libraries. My main class has **`@SpringBootApplication`**, which scans my package and turns on **auto-configuration**. Auto-configuration looks at the classpath and creates Tomcat, the `DispatcherServlet`, a `DataSource`, JPA and Jackson for me, and **backs off** if I define my own. I adjust everything in **`application.properties`**, with **profiles** for different environments, and read my own settings with **`@ConfigurationProperties`**. My data layer is an **`@Entity`** plus a **`JpaRepository` interface**. I protect my API with **validation**, load starting data with a **`CommandLineRunner`**, watch it with **Actuator**, and ship it as **one runnable jar**."

---

## The project: Course Registration API (port 8087)

| Method + URL | What it does | Status |
|---|---|---|
| `GET /courses` | list all | 200 |
| `GET /courses/{id}` | get one | 200 / 404 |
| `GET /courses/search?title=java` | derived query | 200 |
| `POST /courses` | create (validated) | 201 / 400 / 409 (duplicate code) |
| `POST /courses/{id}/enroll` | take a seat | 200 / 404 / 409 (full) |
| `DELETE /courses/{id}` | delete | 204 / 404 |

Compare with Topic 4: **no** `TomcatLauncher`, **no** `@EnableWebMvc`, **no** `DataSource` bean, **no** SQL, and yet it uses a real database.

---

# ✅ Test Yourself

## Part A — Quick check

1. Is Spring Boot a replacement for the Spring Framework? Explain.
2. Which three annotations does `@SpringBootApplication` combine?
3. What is a starter? Name three and what they bring.
4. Explain auto-configuration in one sentence. What does "backs off" mean?
5. Where does Tomcat come from in a Boot app? How do you change its port?
6. What's the difference between `application.properties` and `application-prod.properties`?
7. What's the advantage of `@ConfigurationProperties` over many `@Value`s?
8. What does `@Entity` mean? What does `JpaRepository<Course, Long>` give you for free?
9. What SQL do you think `findByTeacherAndCapacityGreaterThan(String t, int c)` produces?
10. What happens when a request with an empty `title` hits `@Valid @RequestBody CourseRequest`?
11. When does a `CommandLineRunner` run?
12. What does `/actuator/health` tell you, and who uses it?
13. What is inside a "fat jar"? How do you run it?

## Part B — Explain it like a teacher

14. Explain auto-configuration using a **hotel room** (or any example other than a school).
15. Compare Topic 4's Homework API with Topic 7's Course API. List four things you no longer had to write, and say what does each of them now.

## Part C — Hands-on tasks

16. Run the project, then open `/actuator/conditions`. Find `DataSourceAutoConfiguration` and explain why it matched.
17. Change `school.default-course-capacity` in `application-boot.properties` and create a course without a capacity. What capacity does it get?
18. Add a derived query `List<Course> findByTeacher(String teacher)` and an endpoint `GET /courses/by-teacher?name=...`.
19. Add a `@Size(min = 3)` rule to `title`. Prove it works with a request in `course-api.http`.
20. Enroll in the "Robotics Club" course (capacity 2) three times. What happens on the third request, and which class decides the status code?
21. Build the fat jar and run it with `java -jar`, using `--server.port=9090`.

---

<details>
<summary><b>👩‍🏫 Answer key (click to open)</b></summary>

1. **No.** Boot is built **on top of** Spring. It adds auto-configuration, starters, an embedded server and conventions. The container, DI, MVC, data access and security are still Spring.
2. `@Configuration`, `@ComponentScan`, `@EnableAutoConfiguration`.
3. One dependency that brings a tested group of libraries. E.g. `starter-web` (MVC + Tomcat + Jackson), `starter-data-jpa` (JPA + Hibernate + JDBC), `starter-test` (JUnit + Mockito + AssertJ).
4. Boot automatically creates sensible beans based on what's on the classpath and in your settings. "Backs off" = if you define that bean yourself, Boot doesn't create its own.
5. Tomcat is a library inside your app (it comes with `starter-web`), and Boot starts it. Change the port with `server.port=...`.
6. `application.properties` always loads. `application-prod.properties` only loads when the `prod` profile is active, and its values override the base file.
7. All related settings are in one typed object (with validation and IDE auto-complete). They're easy to inject and test, and there are no scattered string keys.
8. The class maps to a database table. You get `save`, `findAll`, `findById`, `deleteById`, `count`, `existsById`, paging/sorting and more, all implemented by Spring.
9. `SELECT * FROM course WHERE teacher = ? AND capacity > ?`
10. Spring throws `MethodArgumentNotValidException` before the controller method runs. Our `ApiErrorHandler` returns **400** with the field errors.
11. Once, right after the application context has started (all beans ready).
12. Whether the app and its dependencies (like the database) are UP. Load balancers, Kubernetes and cloud platforms use it to route traffic and restart unhealthy apps.
13. Your classes, all dependency jars and the embedded server. Run it with `java -jar file.jar`.
14. A hotel room: you walk in and the bed is made, the Wi-Fi is set up and the AC is on (defaults). If you bring your own pillow, housekeeping doesn't force theirs on you (backing off). You change the thermostat (properties).
15. `TomcatLauncher` (embedded server auto-config), `@EnableWebMvc` (web MVC auto-config), the `DataSource`/`TransactionManager` beans (DataSource + transaction auto-config), and the repository implementation and SQL (Spring Data JPA).
16. It matched because a JDBC driver (H2) and `DataSource` classes are on the classpath, and no `DataSource` bean was defined by us.
17. The new value from the properties file (read through `SchoolProperties`).
18. Repository: `List<Course> findByTeacher(String teacher);`. Controller: `@GetMapping("/by-teacher") List<Course> byTeacher(@RequestParam("name") String name)`.
19. `POST /courses` with `"title":"AI"` → 400 with an error on `title`.
20. The first two succeed (200). The third returns **409 Conflict**: `CourseService` throws `CourseFullException`, and `ApiErrorHandler` maps it to 409.
21. `mvnw clean package -DskipTests`, then `java -jar target/perfect-spring-1.0.0.jar --server.port=9090` and open http://localhost:9090/courses.
</details>
