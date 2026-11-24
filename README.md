# Perfect Spring 🌱 — Learn Spring Step by Step

A beginner-friendly course on the Spring ecosystem, written for **teachers learning alongside their students (ages 17–23)**.
Every idea is explained in plain words, with a **real-life illustration**, a small runnable **example**, a **project**, and a **test-yourself** section with an answer key.

---

## 🗺️ The course map

Follow the topics **in order**. Each one solves a problem the previous one left behind.

| # | Topic | The problem it solves (one line) | Project you build |
|---|---|---|---|
| 1 | [Introduction to Spring](src/main/java/com/perfectspring/topic01_introduction/README.md) | Classes welded together with `new` | School Announcement System |
| 2 | [Spring Core](src/main/java/com/perfectspring/topic02_core/README.md) | Listing every object by hand; sharing, life cycle, settings | School Library |
| 3 | [Spring Data Access](src/main/java/com/perfectspring/topic03_dataaccess/README.md) | Painful JDBC; half-finished database changes | Student Records |
| 4 | [Spring Web MVC](src/main/java/com/perfectspring/topic04_webmvc/README.md) | Answering internet requests without hand-written plumbing | Homework Tracker REST API |
| 5 | [Spring Security](src/main/java/com/perfectspring/topic05_security/README.md) | Anyone can do anything; unsafe passwords | School Portal (students / teachers / admin) |
| 6 | [Spring Testing](src/main/java/com/perfectspring/topic06_testing/README.md) | Slow, forgetful manual checking | Result Checker API + its test suite |
| 7 | [Spring Boot](src/main/java/com/perfectspring/topic07_boot/README.md) | The same set-up written again in every project | Course Registration API (JPA, validation, Actuator) |
| 8 | [Spring Cloud & Microservices](src/main/java/com/perfectspring/topic08_cloud/README.md) | Many services finding and calling each other safely | 3-app system: registry + catalog + enrollment |

One analogy runs through the whole course: **a school**. Classes are classrooms, Spring is the HR office, the database is the records room, Web MVC is the front office, Security is the gate, tests are exams, Boot is a furnished building, and Cloud is a campus of buildings.

---

## 📚 How each topic is organised

Every topic is **one package** that holds everything about it:

```
topicNN_name/
├── README.md     1️⃣ READ FIRST: the problem that existed, the end goal, the step-by-step learning path,
│                    and "How to build the project, step by step" (which file to write first, and what every method does)
├── LESSON.md     2️⃣ The lesson: each concept in plain words + a real-life illustration + code
│                    + "Putting it all together" + ✅ Test Yourself (questions, tasks, answer key)
├── example/      3️⃣ Tiny runnable examples, one idea each (run them in number order)
└── project/      4️⃣ A small project that uses every concept of the topic
```

### Suggested teaching rhythm for each topic
1. **Tell the story of the problem** (README section 1). Students should *feel* the pain first.
2. **State the end goal** (README section 2), so everyone knows where you're going.
3. **Walk the learning path** (README section 3 → LESSON.md), one concept at a time. Each concept answers the question the previous one raised.
4. **Run each example** right after its concept is explained.
5. **Build/run the project** and read its files in the order listed in the lesson.
6. **Test yourself**: Part A (quick check), Part B (explain it like a teacher), Part C (hands-on tasks). Answers are in the collapsible key at the bottom.

> 💡 Open the `.md` files in VS Code and press **Ctrl + Shift + V** for a nicely formatted preview.

---

## 🛠️ Setup (one time)

1. Install **Java 17 or newer** (check with `java -version`). This project was built and tested with Java 23.
2. Install **VS Code** with the **Extension Pack for Java** (Microsoft).
3. Optional but recommended: the **REST Client** extension, to click-and-send the requests in the `.http` files.
4. **You don't need to install Maven.** The project includes the **Maven Wrapper** (`mvnw` / `mvnw.cmd`), which downloads the right Maven automatically the first time.

Open this folder in VS Code and wait for the Java extension to finish importing the project (bottom status bar).

---

## ▶️ How to run things

### Option A — VS Code (easiest)
Open any file with a `main` method and click **Run** above it. For tests, use the **Testing** panel (flask icon) or the green ▶ next to a test.

### Option B — Terminal (in this folder)
```bash
# Run any main class: put its full name after -Dstart-class=
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic01_introduction.example.Example1_TightCoupling

# Run every test in the course
mvnw test

# Build one runnable jar (it starts the Topic 7 app)
mvnw clean package -DskipTests
java -jar target/perfect-spring-1.0.0.jar
```
> On macOS/Linux, type `./mvnw` instead of `mvnw`.
> In Windows PowerShell, `curl` means something else. Use `curl.exe`, Git Bash, the REST Client `.http` files, or Postman.

### Ports used by the web apps
| App | Port |
|---|---|
| Topic 4 examples | 8041, 8042 |
| Topic 4 Homework API | 8084 |
| Topic 5 School Portal | 8085 |
| Topic 6 Result Checker | 8086 |
| Topic 7 Hello Boot example / Course API | 8070 / 8087 |
| Topic 8 registry / catalog / enrollment | 8761 / 8081 (+8091) / 8082 |

Stop a running server with **Ctrl + C**.

---

## 🧩 Things shared by the whole project (good to know)

This is **one Maven project** holding **many apps**, so that each topic can be one package. To keep the apps from getting in each other's way:

| File | What it does |
|---|---|
| [pom.xml](pom.xml) | All libraries for all 8 topics (Spring Boot 3.5.12, Spring Cloud 2025.0.3). |
| [application.properties](src/main/resources/application.properties) | Settings shared by every Spring Boot app. It switches Spring Security and Eureka **off** by default. |
| `application-<profile>.properties` | Each Boot app turns on its own **profile** (`security`, `testing`, `boot`, `registry`, `catalog`, `enrollment`), and its file holds that app's settings. Topic 7 explains profiles. |
| `src/main/resources/topic02/`, `topic03/` | Properties and SQL files used by those topics. |
| [common/JsonOnlyWebConfig.java](src/main/java/com/perfectspring/common/JsonOnlyWebConfig.java) | A small helper that makes every app answer in JSON (the Eureka libraries would otherwise make browsers receive XML). |
| `src/test/java/...` | Tests, in the same packages as the code they test (Topic 6 explains why). |

> In a real company, each app (and certainly each microservice in Topic 8) would be its **own** project. Keeping them together here just makes the course easy to open and navigate.

---

## ✅ Verified

- `mvnw test` runs **45 tests**, all green. They cover Topic 5 security rules, Topic 6 unit/slice/integration tests, and the Topic 7 API.
- Every example and project was run and its output checked against the lesson, including the Topic 8 system (registration, load balancing across two catalogs, fallback when the catalog is down, and recovery).
