# Lesson 6 — Spring Testing

**Goal of this lesson:** prove your code works automatically, in seconds, every time you change it.

---

## The big picture story: "Exams and Rehearsals"

| Real life | Testing |
|---|---|
| A **car crash test** before customers drive it | Running tests **before** users touch the app |
| A short **quiz** on ONE topic for ONE student | A **unit test** (one class, alone) |
| The **marking scheme**: "expected answer: 42" | An **assertion** (`assertEquals(42, result)`) |
| Set the desk → student writes → teacher marks | **Arrange → Act → Assert** |
| A **stand-in actor** who reads the other lines during rehearsal | A **mock** (Mockito) |
| Telling the stand-in exactly what to say | `when(...).thenReturn(...)` |
| Checking the actor actually spoke to the stand-in | `verify(...)` |
| A **full dress rehearsal** with the whole cast on stage | An **integration test** (`@SpringBootTest`) |
| Rehearsing **only the choir** | A **slice test** (`@WebMvcTest`) |
| A **pretend visitor** who walks through the front office without the gate opening | **MockMvc** |
| Many quizzes, a few rehearsals, one final exam | The **test pyramid** |

---

## Concept 1 — Why automated tests?

**Plain meaning:** an automated test is **code that checks other code**. You write it once and run it thousands of times for free.

**Real-life illustration:** car makers **crash-test** every new model, and they re-run the tests whenever they change the design. They don't wait for customers to find out if the airbags work.

**Key word: regression.** A *regression* is when something that **used to work** stops working after a change. Tests catch regressions immediately.

**Hold on to:** tests are your **safety net**. With good tests you can change code **without fear**.

---

## Concept 2 — JUnit 5 basics

**Plain meaning:** **JUnit** is *the* testing library for Java. A test is a method marked **`@Test`**. Inside it, **assertions** check results. If an assertion fails, the test turns red.

```java
class ScoreCalculatorTest {

    @Test
    void averageOfThreeScores() {
        ScoreCalculator calculator = new ScoreCalculator();
        double average = calculator.average(List.of(80, 90, 100));
        assertEquals(90.0, average);          // expected, actual
    }

    @Test
    void emptyListIsNotAllowed() {
        ScoreCalculator calculator = new ScoreCalculator();
        assertThrows(IllegalArgumentException.class, () -> calculator.average(List.of()));
    }
}
```

| Tool | Meaning |
|---|---|
| `@Test` | "This method is a test" |
| `assertEquals(expected, actual)` | Must be equal |
| `assertTrue(x)` / `assertFalse(x)` | Must be true / false |
| `assertThrows(Ex.class, () -> ...)` | This code must throw that exception |
| `@BeforeEach` | Run before **every** test (fresh set-up) |
| `@DisplayName("...")` | A human-friendly name in the report |
| `@ParameterizedTest` | The same test with many inputs |

We also use **AssertJ**, which reads like English: `assertThat(grade).isEqualTo("A");`

👉 Read and run `src/test/java/.../example/Example1_FirstJUnitTest.java`

**Hold on to:** **test the expected case, the edge cases (0, 100, empty) and the error cases.**

---

## Concept 3 — The AAA pattern and good names

**Plain meaning:** every good test has three parts:

```java
@Test
void scoreOf70IsGradeA() {
    // ARRANGE: set up what you need
    ScoreCalculator calculator = new ScoreCalculator();

    // ACT: do the ONE thing you're testing
    String grade = calculator.grade(70);

    // ASSERT: check the result
    assertThat(grade).isEqualTo("A");
}
```

**Real-life illustration:** an exam: **Arrange** (seat the student, hand out the paper) → **Act** (the student answers) → **Assert** (the teacher marks it against the marking scheme).

**Good test names** describe *behaviour*: `scoreBelow40IsFail()` beats `test3()`. When it fails, the name already tells you what broke.

**Hold on to:** **one test = one behaviour.** If the name needs "and", split it into two tests.

---

## Concept 4 — Mocks with Mockito

**Plain meaning:** the class you want to test often **depends** on other things (a repository, an email sender). In a *unit* test you don't want the real ones: they're slow, need a database, or send real emails. You use a **mock**: a fake object whose answers **you script**.

**Real-life illustration:** in a drama rehearsal, the lead actor is missing. A **stand-in** reads their lines **exactly as the director instructs**. Afterwards the director can ask: *"Did the other actor actually speak to the stand-in?"*

```java
@ExtendWith(MockitoExtension.class)          // switch Mockito on
class ReportCardServiceTest {

    @Mock ScoreSource scoreSource;            // the stand-in
    @InjectMocks ReportCardService service;   // the real class, with the stand-in plugged in

    @Test
    void buildsReportFromScores() {
        when(scoreSource.scoresFor("ada")).thenReturn(List.of(70, 80, 90));   // script the stand-in

        String report = service.reportFor("ada");                              // act

        assertThat(report).contains("average 80.0");                           // assert the result
        verify(scoreSource).scoresFor("ada");                                  // assert the interaction
    }
}
```

| Mockito tool | Meaning |
|---|---|
| `@Mock` | Create a fake |
| `@InjectMocks` | Create the real class and inject the fakes into it |
| `when(x.call()).thenReturn(v)` | "When asked this, answer that" |
| `when(...).thenThrow(ex)` | "When asked this, fail like this" |
| `verify(x).call()` | "Check that this was called" |
| `verify(x, never()).call()` | "Check that this was NOT called" |

👉 Read and run `Example2_MockitoTest.java`

**Hold on to:** **a mock lets you test ONE class in complete isolation, fast.**

---

## Concept 5 — Dependency Injection makes testing easy

**Plain meaning:** remember Topics 1 and 2: *ask for your dependencies in the constructor.* Here's the payoff. In a test you can simply write:

```java
ReportCardService service = new ReportCardService(fakeScoreSource);
```

No Spring, no database, no magic. If the class had `new DatabaseScoreSource()` welded inside it (**tight coupling**), you could **never** swap in a fake.

**Real-life illustration:** a phone with a **USB-C port** can be tested with any charger. A phone with a **welded cable** can only be tested with that exact cable.

**Hold on to:** **if a class is hard to test, it's usually tightly coupled.** Tests push you towards good design.

---

## Concept 6 — Spring integration tests

**Plain meaning:** unit tests check classes **alone**. But you also need to check that **Spring wires everything correctly**: scanning finds the beans, `@Value`s are filled, the right `@Qualifier` is used. An **integration test** starts a **real Spring container** inside the test.

| Annotation | What it does |
|---|---|
| `@SpringJUnitConfig(MyConfig.class)` | Start a plain Spring container with this config (Topics 1–3 style) |
| `@SpringBootTest` | Start the **whole Spring Boot app** (all beans, all auto-configuration) |
| `@Autowired` (in a test) | Inject beans from that container into the test class |

```java
@SpringJUnitConfig(ExampleConfig.class)
class Example3_SpringContextTest {
    @Autowired ReportCardService service;   // a real bean, wired by Spring

    @Test
    void springWiresTheService() {
        assertThat(service.reportFor("ada")).contains("ada");
    }
}
```

**Real-life illustration:** a **full dress rehearsal**: the whole cast, costumes and lights, all together, to make sure the pieces fit.

**Hold on to:** integration tests are **slower** (they start Spring), so you write **fewer** of them.

---

## Concept 7 — Web tests with MockMvc

**Plain meaning:** **MockMvc** lets you send **pretend HTTP requests** to your controllers and check the responses (status, JSON) **without starting Tomcat**. It goes through the real `DispatcherServlet`, mappings and JSON conversion.

- **`@WebMvcTest(ResultController.class)`** starts **only the web layer** (controllers, JSON, error handling). Services are **not** created, so you provide them with **`@MockitoBean`**.
- **`@SpringBootTest` + `@AutoConfigureMockMvc`** starts **everything**, and MockMvc calls the real full stack.

```java
@WebMvcTest(ResultController.class)
class ResultControllerTest {

    @Autowired MockMvc mockMvc;               // the pretend visitor
    @MockitoBean GradingService gradingService;  // a mock bean placed into the Spring container

    @Test
    void returnsResultAsJson() throws Exception {
        when(gradingService.resultFor(1L)).thenReturn(new ResultCard(1L, "Ada", 85.0, "A", true));

        mockMvc.perform(get("/results/1"))                   // act: send GET /results/1
               .andExpect(status().isOk())                    // assert: 200
               .andExpect(jsonPath("$.name").value("Ada"))    // assert: JSON field
               .andExpect(jsonPath("$.grade").value("A"));
    }
}
```

`jsonPath("$.name")` means *"in the JSON, the field `name` at the top level"*.

**Real-life illustration:** a **pretend visitor** who walks through the front office during a rehearsal. The receptionist and offices act for real, but the building's front gate never actually opens to the street.

**Hold on to:** **`@WebMvcTest` = web layer only (fast, mock the services). `@SpringBootTest` = everything (slower, most realistic).**

---

## Concept 8 — The test pyramid

```
          /\        End-to-end (real browser, real server)  → very few, slow
         /  \
        /----\      Integration (@SpringBootTest, @WebMvcTest) → some
       /      \
      /--------\    Unit tests (JUnit + Mockito)              → MANY, super fast
```

**Real-life illustration:** a school has **many quick quizzes**, **some rehearsals**, and **one final exam**. If you only had final exams, you'd discover problems far too late.

**Hold on to:** **most of your tests should be fast unit tests.** Add integration tests for the wiring and the web layer.

---

## Putting it all together (tell it as one story)

> "Clicking around by hand is slow and forgetful, so I write **automated tests** with **JUnit**. Each test **Arranges, Acts and Asserts**, and has a name that describes one behaviour. To test a class alone I replace its dependencies with **Mockito mocks** and script their answers. This is easy because I used **constructor injection**. To check the Spring wiring I write **integration tests** with `@SpringJUnitConfig` or `@SpringBootTest`. To test my controllers without starting a server I use **MockMvc** with `@WebMvcTest`. I follow the **test pyramid**: lots of unit tests, fewer integration tests. And `mvnw test` runs everything in seconds."

---

## The project: Result Checker API (port 8086)

`GET /results/{studentId}` → `{ "studentId":1, "name":"Ada", "average":85.0, "grade":"A", "passed":true }`

Grading rules (in `GradingService`): **A ≥ 70, B ≥ 60, C ≥ 50, D ≥ 45, E ≥ 40, F < 40**. Pass = 40 or more.

| Test | Level | Starts Spring? | Speed |
|---|---|---|---|
| `GradingServiceTest` | Unit + Mockito | ❌ | ⚡ fastest |
| `ResultControllerTest` | Web slice (`@WebMvcTest`) | Web layer only | 🚀 fast |
| `ResultCheckerIntegrationTest` | Full (`@SpringBootTest`) | ✅ everything | 🐢 slowest |

Bonus: `src/test/java/.../topic05_security/project/SchoolPortalSecurityTest.java` tests the Topic 5 security rules (401/403/200) with MockMvc.

---

# ✅ Test Yourself

## Part A — Quick check

1. What is a regression?
2. What does `@Test` mark? What does `assertEquals(5, x)` check?
3. What are the three parts of the AAA pattern?
4. Why is `test1()` a bad test name? Suggest a better one for a test that checks 39 is a fail.
5. What is a mock? Why use one in a unit test?
6. What's the difference between `when(...).thenReturn(...)` and `verify(...)`?
7. What does `@InjectMocks` do?
8. Why does constructor injection make testing easier?
9. What's the difference between `@WebMvcTest` and `@SpringBootTest`?
10. Why do we need `@MockitoBean` in a `@WebMvcTest`?
11. What does `jsonPath("$.grade").value("A")` check?
12. Draw (or describe) the test pyramid. Why is it shaped like that?
13. Where must test files live in a Maven project?

## Part B — Explain it like a teacher

14. Explain mocks using an example that **isn't** acting (e.g. a flight simulator, a sparring partner in boxing, a mannequin).
15. A classmate says "testing is a waste of time, I'll just run the app". Convince them.

## Part C — Hands-on tasks

16. Run `mvnw test`. How many tests pass?
17. In `GradingService`, break the rules on purpose: change `>= 70` to `> 70`. Run the tests. Which test fails and what does the message say? Undo your change.
18. Add a test to `Example1_FirstJUnitTest` proving that a score of `100` is grade `A`.
19. In `GradingServiceTest`, add a test proving that a student with scores `[30, 35]` has **not** passed.
20. Add a test to `ResultControllerTest` for `GET /results/999` returning **404** (tip: make the mock `thenThrow(new StudentNotFoundException(999L))`).

---

<details>
<summary><b>👩‍🏫 Answer key (click to open)</b></summary>

1. When something that used to work breaks after a change.
2. A method that JUnit should run as a test. It checks that `x` equals `5`. Otherwise the test fails.
3. Arrange (set up), Act (do the one thing), Assert (check the result).
4. It doesn't say what behaviour is being tested, so a failure tells you nothing. Better: `scoreOf39IsAFail()`.
5. A fake object whose answers you control. It isolates the class under test from slow or unpredictable dependencies (databases, networks), so tests are fast and reliable.
6. `when/thenReturn` **scripts** what the mock answers (setup). `verify` **checks** that the mock was called (an assertion about interaction).
7. It creates the real object under test and injects the `@Mock` fields into it (through the constructor if possible).
8. You can create the class with `new MyService(mockDependency)` in a test. No Spring is needed and nothing is welded inside.
9. `@WebMvcTest` starts only the web layer (controllers, JSON, advice). `@SpringBootTest` starts the whole application context.
10. `@WebMvcTest` does not create services or repositories, but the controller needs them. `@MockitoBean` puts a mock version into the Spring container.
11. That the response JSON has a top-level field `grade` with the value `"A"`.
12. Wide bottom: many unit tests. Middle: some integration tests. Narrow top: few end-to-end tests. Unit tests are fast and cheap, so you can have many. End-to-end tests are slow and fragile.
13. In `src/test/java`, usually in the same package as the class being tested.
14. A boxer's **sparring partner** throws exactly the punches the coach asks for, so the boxer can practise one defence safely. That's a mock: a controlled stand-in.
15. "Running the app checks one thing, once, today. Tests check everything, every time, in seconds, forever. They catch the bug you introduce next month in a part you forgot to click."
16. All tests pass (green). The exact number is shown at the end of the Maven output.
17. `averageOfSeventyIsGradeA` (or similar) fails, with a message like `expected: "A" but was: "B"`.
18. `@Test void hundredIsGradeA() { assertThat(calculator.grade(100)).isEqualTo("A"); }`
19. `when(repository.findById(3L)).thenReturn(Optional.of(new StudentScores(3L, "X", List.of(30, 35))));` then `assertThat(service.resultFor(3L).passed()).isFalse();`
20. `when(gradingService.resultFor(999L)).thenThrow(new StudentNotFoundException(999L)); mockMvc.perform(get("/results/999")).andExpect(status().isNotFound());`
</details>
