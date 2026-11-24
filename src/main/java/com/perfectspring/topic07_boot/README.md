# Topic 7 — Spring Boot

> Read this file first. Then read [LESSON.md](LESSON.md). Then run `example/` in order, then `project/`.

---

## 1. The problem that existed

Look back at everything **you** had to set up by hand in Topics 1–4:

| Topic | What you wrote yourself |
|---|---|
| 2 | `@Configuration` + `@ComponentScan` + `@PropertySource` |
| 3 | A `DataSource` bean, a `JdbcTemplate` bean, a `TransactionManager` bean, `@EnableTransactionManagement` |
| 4 | Create Tomcat, set a port, create a web container, create the `DispatcherServlet`, map it to `/`, `@EnableWebMvc` |
| All | Choose library versions in `pom.xml` that work together (and hope they do) |

That's **set-up code**, not business code, and every Spring project in the world was writing the **same** set-up again and again. Teams lost days on:

1. **Version hell.** "Spring 5.3 with Hibernate 6.1 and Jackson 2.9... why does it crash?"
2. **Boilerplate configuration.** The same `DataSource`/`DispatcherServlet` beans copied into every project.
3. **Deploying was heavy.** Build a `.war` file, install a separate Tomcat server, copy the file in, configure the server...
4. **Different settings per environment.** Development, testing and production each need different URLs and passwords.

**Spring Boot** (2014) said: *"Most apps need the same sensible set-up. We'll do it for you automatically, and you only change what's different."*

> ⚠️ Spring Boot is **not** a replacement for Spring. It is Spring (Topics 1–6) **plus automatic set-up**. Everything you learned still applies.

---

## 2. The end goal of this topic

> **"I add *starters* to my `pom.xml`. `@SpringBootApplication` turns on component scanning and *auto-configuration*, which looks at what's on the classpath and creates the beans I need (Tomcat, DispatcherServlet, DataSource, JPA, Jackson...). It backs off whenever I define my own. I control it through `application.properties` and *profiles*. With Spring Data JPA I write an *interface* and get a whole repository. And I ship my app as one runnable `.jar`."**

You'll build a **Course Registration API** with a real database (JPA), input validation, typed settings, start-up data and health checks, in fewer lines than Topic 4's in-memory version.

---

## 3. The learning path (one step at a time)

| Step | Learn this | Which raises the question... |
|---|---|---|
| **1** | **The set-up pain** (what we did by hand in Topics 1–4). | "Can a tool do the standard set-up for me?" |
| **2** | **`@SpringBootApplication` + `SpringApplication.run`.** One annotation, one line. | "Where do all the libraries come from?" |
| **3** | **Starters.** Ready-packed groups of libraries whose versions work together. | "Who creates the beans those libraries need?" |
| **4** | **Auto-configuration.** "I see H2 on the classpath, so I'll create a `DataSource`." It backs off if you define your own. | "Where did Tomcat go?" |
| **5** | **The embedded server.** Tomcat lives *inside* your app. | "How do I change the port, the database URL...?" |
| **6** | **`application.properties`, profiles and `@ConfigurationProperties`.** | "With a database already configured, can I skip writing SQL?" |
| **7** | **Spring Data JPA.** `@Entity` + `JpaRepository` = CRUD for free, plus queries from method names. | "How do I stop bad data at the door?" |
| **8** | **Validation.** `@Valid`, `@NotBlank`, `@Min`... | "How do I put starting data in, or run code at start-up?" |
| **9** | **`CommandLineRunner`.** Code that runs once the app has started. | "How do I know the app is healthy in production?" |
| **10** | **Actuator.** `/actuator/health` and friends. | "How do I give my app to someone else?" |
| **11** | **Packaging: the runnable fat jar.** `mvnw package` → `java -jar app.jar`. | Go to Topic 8. |

---

## 4. What is inside this folder

```
topic07_boot/
├── README.md
├── LESSON.md
├── example/
│   ├── ex1_hello/HelloBootApp.java               (Steps 2-5: a whole web app in ~20 lines, port 8070)
│   └── ex2_autoconfig/AutoConfigPeekApp.java     (Step 4: SEE the beans Boot created for you)
└── project/  "Course Registration API" (port 8087)
    ├── CourseRegistrationApp.java   (@SpringBootApplication)
    ├── SchoolProperties.java        (@ConfigurationProperties: typed settings)
    ├── Course.java                  (@Entity = a table)
    ├── CourseRepository.java        (extends JpaRepository: no code needed!)
    ├── CourseRequest.java           (incoming data + validation rules)
    ├── CourseService.java           (business rules + @Transactional)
    ├── CourseController.java        (REST endpoints)
    ├── ApiErrorHandler.java         (400 / 404 / 409 replies)
    ├── CourseNotFoundException.java, CourseFullException.java, DuplicateCourseCodeException.java
    └── StartupDataLoader.java       (CommandLineRunner: starting data)
└── course-api.http

src/main/resources/application-boot.properties    (the project's settings)
src/test/java/.../topic07_boot/project/CourseRegistrationApiTest.java
```

---

## 5. How to build the project, step by step

**The rule (the standard Spring Boot recipe):** starters in `pom.xml` → settings in `application.properties` → **entity** → **repository interface** → request class with validation → exceptions → **service** → **controller** → error handler → start-up data → main class. Notice what's *missing* compared with Topics 3–4: no `DataSource`, no `TransactionManager`, no Tomcat, no `@EnableWebMvc`. Boot auto-configures all of them.

### Step 1 — `pom.xml` (choose the starters)
**Why first:** what's on the classpath decides what Boot auto-configures.
- `spring-boot-starter-web`, `-data-jpa`, `-validation`, plus `h2` (the database) and Actuator (brought in here by the Topic 8 libraries).

### Step 2 — `application-boot.properties` (the control panel)
**Why now:** port, database, JPA and Actuator settings, plus **our own** `school.*` settings.
- `server.port`, `spring.datasource.*`, `spring.jpa.*`, `spring.h2.console.enabled`, `management.endpoints...`, `school.name`, `school.default-course-capacity`.

### Step 3 — `SchoolProperties.java` (`@ConfigurationProperties(prefix = "school")`)
**Why now:** turns the `school.*` settings into one typed object that the service and loader can use.
- `name()`: the school name.
- `defaultCourseCapacity()`: used when a new course gives no capacity.

### Step 4 — `Course.java` (`@Entity`: a database table)
**Why now:** everything is about courses. Hibernate creates the `course` table from this class.
- `@Id @GeneratedValue id`: the primary key, numbered by the database.
- **Empty constructor** (`protected`): JPA needs it to build objects from rows.
- **Constructor** `Course(code, title, teacher, capacity)`: creates a new course.
- `isFull()`: whether there are no seats left.
- `enrollOneStudent()`: takes one seat.
- Getters (`getCode()`, `getTitle()`, ...) and `getSeatsLeft()`: these become the JSON fields.

### Step 5 — `CourseRepository.java` (interface extending `JpaRepository<Course, Long>`)
**Why now:** it needs the entity. **You write no implementation**: Spring writes it.
- Free methods: `save`, `saveAll`, `findAll`, `findById`, `existsById`, `deleteById`, `count`...
- `findByTitleContainingIgnoreCase(text)`: a **derived query**, where the method name becomes SQL.
- `existsByCode(code)`: a derived query that asks "is this code already used?"

### Step 6 — `CourseRequest.java` (what clients send + validation rules)
**Why now:** defines the incoming JSON and the "form checker" rules.
- `@NotBlank @Size(max = 10) code`, `@NotBlank title`, `teacher`, `@Min(1) @Max(200) capacity` (optional).

### Step 7 — `CourseNotFoundException`, `CourseFullException`, `DuplicateCourseCodeException`
**Why now:** the service needs clear names for its problems (404, 409, 409).
- Each **constructor** builds a readable message.

### Step 8 — `CourseService.java` (`@Service`: business rules)
**Why now:** it uses the repository, the settings and the exceptions.
- `findAll()`: all courses.
- `findOne(id)`: one course, or `CourseNotFoundException`.
- `searchByTitle(text)`: uses the derived query.
- `create(request)` (`@Transactional`): rejects duplicate codes, uses the default capacity if none was given, and saves.
- `enroll(courseId)` (`@Transactional`): rejects a full course, otherwise takes a seat. There's **no `save()` call**, because JPA *dirty checking* saves the change.
- `delete(id)` (`@Transactional`): deletes the course, or throws `CourseNotFoundException`.

### Step 9 — `CourseController.java` (`@RestController`, exactly as in Topic 4)
- `all()`: `GET /courses`.
- `one(id)`: `GET /courses/{id}`.
- `search(title)`: `GET /courses/search?title=`.
- `create(@Valid request)`: `POST /courses`. **`@Valid`** runs the validation rules first. Replies 201.
- `enroll(id)`: `POST /courses/{id}/enroll`.
- `delete(id)`: `DELETE /courses/{id}`. Replies 204.

### Step 10 — `ApiErrorHandler.java` (`@RestControllerAdvice`)
- `invalid(MethodArgumentNotValidException)`: **400** with one message per broken field.
- `notFound(...)`: **404**.
- `conflict(...)`: **409**, for a full course or a duplicate code.

### Step 11 — `StartupDataLoader.java` (`CommandLineRunner`)
**Why now:** it needs the repository and the settings, and runs **after** the app has started.
- `run(...)`: saves three starting courses and prints a welcome banner.

### Step 12 — `CourseRegistrationApp.java` (the main class)
**Why last:** one class switches everything on.
- `@SpringBootApplication`: `@Configuration` + `@ComponentScan` + `@EnableAutoConfiguration`.
- `@ConfigurationPropertiesScan`: finds `SchoolProperties`.
- `main()`: starts Boot with the `boot` profile.

### Step 13 — `course-api.http` and `CourseRegistrationApiTest.java` (try it and prove it)
- Requests for every endpoint and every error, plus Actuator. Tests for start-up data, the derived query, the default capacity, validation (400), a full course (409) and health (UP).

> 🔁 **The Spring Boot recipe:** Starters → `application.properties` → `@ConfigurationProperties` → `@Entity` → `JpaRepository` interface → request class + validation → exceptions → `@Service` (`@Transactional`) → `@RestController` (`@Valid`) → `@RestControllerAdvice` → `CommandLineRunner` → `@SpringBootApplication` main → `.http` + tests.

---

## 6. How to run

```bash
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic07_boot.example.ex1_hello.HelloBootApp
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic07_boot.example.ex2_autoconfig.AutoConfigPeekApp
mvnw spring-boot:run      # the project is the default start-class in pom.xml
```

Then try:
- http://localhost:8087/courses (the API)
- http://localhost:8087/actuator/health (is the app healthy?)
- http://localhost:8087/actuator/conditions (Boot's report of **which auto-configurations ran, and why**)
- http://localhost:8087/h2-console (look inside the database: JDBC URL `jdbc:h2:mem:courses`, user `sa`, no password)

**Build one runnable file and run it anywhere Java is installed:**
```bash
mvnw clean package -DskipTests
java -jar target/perfect-spring-1.0.0.jar
```
