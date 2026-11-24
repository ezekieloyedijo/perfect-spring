# Topic 6 — Spring Testing

> Read this file first. Then read [LESSON.md](LESSON.md). Then read and run the tests in `src/test/java/com/perfectspring/topic06_testing/`.

---

## 1. The problem that existed

How do you know your code works? Most beginners **run the app and click around**:
"I'll start the server, open Postman, send a request, look at the answer... looks fine!"

That's **manual testing**, and it breaks down quickly:

1. **It's slow.** Checking 50 endpoints by hand after every change takes hours.
2. **People forget.** You changed the grading rule, re-checked "A", forgot to re-check "F"... and "F" is now broken.
3. **Old bugs come back.** A fix in one place silently breaks something that used to work. This is called a **regression**.
4. **Real dependencies get in the way.** To check one small rule, you need the database running, the network up, the email server online...
5. **Nobody trusts a change.** Without a safety net, everyone becomes afraid to improve the code.

**Automated tests** are small programs that check your code *for* you, in seconds, every time. **Spring Testing** (with JUnit 5, Mockito and MockMvc) makes it easy to test single classes, the Spring wiring, and web endpoints, **without starting a real server**.

---

## 2. The end goal of this topic

> **"I write JUnit tests that Arrange, Act and Assert. I test one class alone (a **unit test**) and replace its dependencies with **mocks** (Mockito). I test that Spring wires my beans correctly (**`@SpringJUnitConfig` / `@SpringBootTest`**). I test my controllers with **MockMvc** without starting a real server (**`@WebMvcTest`**). And I run all of it with one command: `mvnw test`."**

You'll test a **Result Checker API** at three levels: unit, web slice, and full integration.

---

## 3. The learning path (one step at a time)

| Step | Learn this | Which raises the question... |
|---|---|---|
| **1** | **Why automated tests?** Manual checking is slow and forgetful. | "What tool writes and runs tests in Java?" |
| **2** | **JUnit 5 basics.** `@Test`, `assertEquals`, `assertThrows`, running tests. | "How should a good test be shaped?" |
| **3** | **The AAA pattern + good names.** Arrange → Act → Assert. | "What if the class I'm testing needs a database or another service?" |
| **4** | **Mocks with Mockito.** Fake stand-ins: `@Mock`, `when(...).thenReturn(...)`, `verify(...)`. | "Why was constructor injection (Topic 2) so important?" |
| **5** | **DI makes testing easy.** Just pass mocks into the constructor. | "How do I check that Spring itself wires everything correctly?" |
| **6** | **Spring integration tests.** `@SpringJUnitConfig`, `@SpringBootTest`, `@Autowired` in tests. | "How do I test controllers without starting Tomcat?" |
| **7** | **Web tests with MockMvc.** `@WebMvcTest`, `@MockitoBean`, `perform(get(...))`, `jsonPath`. | "How many of each kind of test should I write?" |
| **8** | **The test pyramid.** Many unit tests, fewer integration tests, very few end-to-end tests. | Go to Topic 7. |

---

## 4. What is inside this folder (and its test twin)

Code being tested lives in `src/main/java`. Tests live in **`src/test/java` in the same package**.
That's Maven's rule: test code is kept apart, so it never ends up inside your real app.

```
src/main/java/com/perfectspring/topic06_testing/
├── README.md, LESSON.md
├── example/
│   ├── ScoreCalculator.java       (pure logic: no dependencies)
│   ├── ScoreSource.java           (an interface: "somewhere scores come from")
│   ├── ReportCardService.java     (depends on ScoreSource, so it needs a mock in tests)
│   └── ExampleConfig.java         (Spring config for the context test)
└── project/  "Result Checker API" (port 8086)
    ├── ResultCheckerApp.java
    ├── StudentScores.java         (what is stored: name + raw scores)
    ├── ResultCard.java            (what the API returns: average, grade, passed)
    ├── ScoreRepository.java + InMemoryScoreRepository.java
    ├── GradingService.java
    ├── ResultController.java
    └── StudentNotFoundException.java

src/test/java/com/perfectspring/topic06_testing/
├── example/
│   ├── Example1_FirstJUnitTest.java      (Steps 2-3)
│   ├── Example2_MockitoTest.java         (Steps 4-5)
│   └── Example3_SpringContextTest.java   (Step 6)
└── project/
    ├── GradingServiceTest.java           (UNIT test with a mock: fastest)
    ├── ResultControllerTest.java         (WEB SLICE test: @WebMvcTest + MockMvc)
    └── ResultCheckerIntegrationTest.java (FULL test: @SpringBootTest)
```

---

## 5. How to build the project, step by step

**The rule:** write the app in layers as usual, but **put an interface in front of every dependency** you'll want to fake (the repository). Then write the tests **from the bottom of the pyramid up**: unit test → web slice test → full integration test.

### Part 1 — the app (`src/main/java/.../topic06_testing/project/`)

### Step 1 — `StudentScores.java` and `ResultCard.java` (the data)
**Why first:** they're what goes **in** (raw scores) and what comes **out** (the result).
- `StudentScores(studentId, name, scores)`: what the repository stores.
- `ResultCard(studentId, name, average, grade, passed)`: what the API returns as JSON.

### Step 2 — `ScoreRepository.java` (interface)
**Why now:** an **interface** is what lets tests swap the real storage for a mock.
- `findById(studentId)`: the promise "give me a student's scores, or nothing".

### Step 3 — `InMemoryScoreRepository.java` (`@Repository`: the real implementation)
**Why now:** the app needs real data when it runs (and in the full integration test).
- `findById(studentId)`: looks the student up in a fixed map (Ada, Tunde, Musa).

### Step 4 — `StudentNotFoundException.java` (a named problem)
- **Constructor**: builds the message `Student #id not found`.
- `@ResponseStatus(HttpStatus.NOT_FOUND)`: if it escapes a controller, Spring replies **404**.

### Step 5 — `GradingService.java` (`@Service`: the rules we want to prove)
**Why now:** this is what most of our tests check. It gets its dependency through the **constructor**, so a mock can be passed in.
- **Constructor** `GradingService(ScoreRepository)`: receives the repository (real or mock).
- `resultFor(studentId)`: loads the scores (or throws `StudentNotFoundException`), calculates the **average**, the **grade** and **passed** (40 or more), and returns a `ResultCard`.
- `gradeFor(average)`: the grading rules: A ≥ 70, B ≥ 60, C ≥ 50, D ≥ 45, E ≥ 40, F below 40.

### Step 6 — `ResultController.java` (`@RestController`)
- `result(studentId)`: `GET /results/{studentId}`. It asks the service and returns the `ResultCard` as JSON.

### Step 7 — `ResultCheckerApp.java` + `application-testing.properties` (the launcher)
- `main()`: starts Spring Boot with the `testing` profile (port 8086).

### Part 2 — the tests (`src/test/java/.../topic06_testing/project/`), bottom of the pyramid first

### Step 8 — `GradingServiceTest.java` (UNIT test: fastest, no Spring)
- `@Mock ScoreRepository` + `@InjectMocks GradingService`: the real service with a fake repository.
- `averageOfSeventyIsGradeA()`: scripts the mock with `when(...).thenReturn(...)` and checks average, grade and passed.
- `averageOfFortyIsAPass()`: checks the pass boundary.
- `unknownStudentThrowsNotFound()`: checks the exception and its message.
- `gradeBoundariesAreCorrect()`: pure logic, no mock needed.

### Step 9 — `ResultControllerTest.java` (WEB SLICE test: only the web layer)
- `@WebMvcTest(ResultController.class)`: starts only the web layer. `@MockitoBean GradingService` supplies a mock service.
- `returnsTheResultCardAsJson()`: `mockMvc.perform(get("/results/1"))`, then checks the status and JSON fields with `jsonPath`.
- `textInsteadOfANumberIsABadRequest()`: `/results/abc` → 400.

### Step 10 — `ResultCheckerIntegrationTest.java` (FULL test: everything real)
- `@SpringBootTest` + `@AutoConfigureMockMvc`: the whole app, nothing mocked.
- `adaGetsAnAWithTheRealData()`, `musaFails()`, `unknownStudentIs404()`: check real answers end to end.

> 🔁 **The pattern to remember:** Data → **interface** for each dependency → real implementation → exceptions → `@Service` (constructor injection) → `@RestController` → `main`. Then tests: **unit (Mockito) → `@WebMvcTest` (MockMvc + `@MockitoBean`) → `@SpringBootTest`**.
>
> 💡 You can also write each test **before** its code (*Test-Driven Development*): write a failing test, write just enough code to pass it, tidy up, repeat.

---

## 6. How to run the tests

**VS Code:** open the **Testing** panel (the flask icon 🧪), or click the green ▶ next to any `@Test`.

**Terminal:**
```bash
mvnw test                                   # run ALL tests in the project
mvnw test -Dtest=Example1_FirstJUnitTest    # run one test class
mvnw test -Dtest="GradingServiceTest#*Grade*"   # run matching test methods
```

Green = passed ✅, red = failed ❌. A failing test tells you **exactly** which expectation was wrong.

You can also run the app itself:
```bash
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic06_testing.project.ResultCheckerApp
# then open http://localhost:8086/results/1
```
