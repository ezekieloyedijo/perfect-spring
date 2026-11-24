# Lesson 2 — Spring Core

**Goal of this lesson:** learn exactly how Spring's "HR office" (the container) finds, creates, connects, shares and looks after your objects.

---

## The big picture story: "The School HR Office, in full detail"

In Topic 1 the principal wrote a **hiring plan** listing every staff member by hand (`@Bean`).
In Topic 2 the school gets smarter:

| Real life | Spring Core |
|---|---|
| Staff wear a **name badge** that says "I work here" | `@Component`, `@Service`, `@Repository` |
| HR **walks the corridors** collecting everyone with a badge | **Component scanning** (`@ComponentScan`) |
| A classroom posts "I need a Maths teacher" on its door. HR assigns one | **Autowiring** via the constructor |
| Two Maths teachers apply. HR uses the **"head of department"** unless a class asks **by name** | `@Primary` / `@Qualifier("name")` |
| One **class register** shared by everyone vs. a **fresh exam paper** for each student | **Singleton** vs **Prototype** scope |
| A teacher's **first-day orientation** and **handing back keys** on the last day | `@PostConstruct` / `@PreDestroy` |
| The **school rule book** on the notice board ("Max 3 books per student") | `.properties` file + `@Value` |
| A **term-time timetable** vs. a **holiday timetable** | `@Profile("term")` / `@Profile("holiday")` |

---

## Concept 1 — Recap: the container and beans

- The **container** (`ApplicationContext`) creates and manages objects.
- A **bean** is any object the container manages.
- Until now we described every bean with a `@Bean` method. That works, but it's a lot of typing.

**Hold on to:** the container is still the boss. In this lesson we only change **how it finds out what to create**.

---

## Concept 2 — `@Component`: classes that register themselves

**Plain meaning:** put `@Component` on a class and it means *"Spring, please create one of me and manage me."* No `@Bean` method needed.

**Real-life illustration:** instead of HR keeping a hand-written list, **every staff member wears a badge**. If you're wearing a badge, you're on the staff.

Spring has **specialised badges**. They behave the same, but they describe the *role*:

| Annotation | Role (school story) | Use it for |
|---|---|---|
| `@Component` | General staff | Any helper class |
| `@Service` | A **teacher** (does the main work) | Business logic |
| `@Repository` | The **records officer** | Classes that read/save data |
| `@Controller` | The **receptionist** | Classes that answer web requests (Topic 4) |

```java
@Service
public class LibraryService { ... }

@Repository
public class BookRepository { ... }
```

**Hold on to:** `@Service`, `@Repository` and `@Controller` are all *kinds of* `@Component`. They help humans read the code (and `@Repository` gets one extra database feature in Topic 3).

---

## Concept 3 — Component scanning

**Plain meaning:** badges are useless if nobody looks for them. `@ComponentScan` tells Spring: *"walk through this package (and its sub-packages) and register every class with a badge."*

**Real-life illustration:** on the first day of term, **HR walks every corridor of one building** and writes down everyone wearing a badge. People in *other* buildings are not counted.

```java
@Configuration
@ComponentScan   // no package given = "scan MY package and everything under it"
public class LibraryConfig { }
```

**Hold on to:** **scanning starts from the package of the config class and goes downwards.** A `@Component` outside that area is invisible to Spring. This is the #1 beginner bug: *"My bean isn't found!"*

👉 Run `example/ex1_componentscan/ComponentScanExample.java`

---

## Concept 4 — Autowiring (constructor injection)

**Plain meaning:** when Spring creates a bean, it looks at the bean's **constructor**. For every parameter, it finds a matching bean and passes it in. This automatic matching is called **autowiring**.

**Real-life illustration:** a classroom puts a sign on its door: *"This room needs: 1 Maths teacher, 1 projector."* HR reads the sign and **delivers both** before the first lesson.

```java
@Service
public class LibraryService {
    private final BookRepository bookRepository;

    // Only one constructor, so Spring uses it automatically. (@Autowired is optional here.)
    public LibraryService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
}
```

**Three ways to inject (know them, prefer the first):**

| Way | Looks like | Verdict |
|---|---|---|
| **Constructor** | `public X(Y y)` | ✅ **Best.** The field can be `final`, you never forget a dependency, and it's easy to test. |
| Setter | `@Autowired void setY(Y y)` | OK for *optional* dependencies |
| Field | `@Autowired private Y y;` | ❌ Avoid. It hides dependencies and is hard to test. |

**Hold on to:** **Spring matches by type.** "Need a `BookRepository`? I have exactly one `BookRepository` bean. Here you go."

---

## Concept 5 — Two candidates? `@Primary` and `@Qualifier`

**Plain meaning:** if a constructor asks for a `Notifier`, and there are **two** `Notifier` beans (`ConsoleNotifier` and `SmsNotifier`), Spring can't guess. It throws an error:
`NoUniqueBeanDefinitionException: expected single matching bean but found 2`.

You fix it in one of two ways:

- **`@Primary`** on one bean: *"If nobody says otherwise, use me."*
- **`@Qualifier("smsNotifier")`** at the injection point: *"I want THAT one, by name."*

**Real-life illustration:** two Maths teachers work at the school. HR normally sends the **Head of Maths** (`@Primary`). But the Further-Maths class has written **"We want Mr. Okafor specifically"** on its door (`@Qualifier`).

```java
@Component @Primary
public class ConsoleNotifier implements Notifier { ... }

@Component("smsNotifier")
public class SmsNotifier implements Notifier { ... }

public Reminder(@Qualifier("smsNotifier") Notifier notifier) { ... } // asks by name
```

**Hold on to:** a bean's **default name** is its class name with a lower-case first letter (`SmsNotifier` → `"smsNotifier"`).

👉 Run `example/ex2_qualifier/QualifierExample.java`

---

## Concept 6 — Bean scopes: singleton vs prototype

**Plain meaning:** the *scope* decides **how many copies** of a bean Spring makes.

| Scope | How many | School story |
|---|---|---|
| **singleton** (the default) | **One** shared copy for the whole app | The **class register**. There is one, and everybody writes in the same book. |
| **prototype** | A **brand-new copy every time** someone asks | **Exam papers**. Each student gets a fresh one. |

```java
@Component                    // singleton by default
public class ClassRegister { }

@Component
@Scope("prototype")           // new object every time
public class ExamPaper { }
```

**Hold on to:** most beans (services, repositories) are **singletons**, so **don't store per-user data in their fields**. All users would share it! (Web apps also have `request` and `session` scopes. You'll meet them later.)

👉 Run `example/ex3_scopes/ScopeExample.java`

---

## Concept 7 — The bean life cycle

**Plain meaning:** every bean goes through a life:
**1. created → 2. dependencies injected → 3. `@PostConstruct` runs → 4. bean is used → 5. `@PreDestroy` runs when the container closes.**

**Real-life illustration:** a new librarian is **hired** (created), **given the keys** (injected), attends **orientation and arranges the shelves** (`@PostConstruct`), **works** all term (used), and on the last day **hands back the keys and locks up** (`@PreDestroy`).

```java
@PostConstruct
void openLibrary()  { System.out.println("Shelves arranged, library open!"); }

@PreDestroy
void closeLibrary() { System.out.println("Lights off, doors locked."); }
```

**Why not do set-up in the constructor?** Inside the constructor, injection may not be finished yet. `@PostConstruct` is guaranteed to run **after** all dependencies and `@Value`s are in place.

👉 Run `example/ex4_lifecycle/LifecycleExample.java`

---

## Concept 8 — External configuration: `@PropertySource` + `@Value`

**Plain meaning:** keep **settings** (numbers, names, URLs) in a **`.properties` text file**, not inside Java code. Spring reads the file and injects values with `@Value("${key}")`.

**Real-life illustration:** the **school rule book on the notice board** says *"Max 3 books per student."* To change the rule, the principal **updates the notice board**. Nobody rebuilds the library.

`src/main/resources/topic02/library.properties`:
```properties
library.name=Unity High School Library
library.max-books-per-student=2
```

```java
@Configuration
@PropertySource("classpath:topic02/library.properties")   // "pin this rule book on the board"
public class LibraryConfig { }

@Service
public class LibraryService {
    public LibraryService(@Value("${library.max-books-per-student}") int maxBooks) { ... }
}
```

**Default values:** `@Value("${school.motto:Learn and Grow}")` means *"use `Learn and Grow` if the key is missing."*

**Hold on to:** `${...}` = "look this key up in the properties". `classpath:` = "look inside `src/main/resources`".

👉 Run `example/ex5_properties/PropertiesExample.java`

---

## Concept 9 — Profiles

**Plain meaning:** a **profile** is a named "situation". Beans marked `@Profile("x")` are only created when profile `x` is **active**.

**Real-life illustration:** the school has a **term timetable** and a **holiday timetable**. Same school, same building. Which timetable is pinned up depends on the season.

```java
@Component @Profile("term")
class TermBells implements BellSchedule { ... }

@Component @Profile("holiday")
class HolidayBells implements BellSchedule { ... }
```

Real uses: a fake email sender in `dev`, the real one in `prod`. An in-memory database for `test`, a real one for `prod`.

👉 Run `example/ex6_profiles/ProfileExample.java`

---

## Putting it all together (tell it as one story)

> "I put **badges** (`@Component`, `@Service`, `@Repository`) on my classes. HR (**the container**) **scans** my package and creates them. When a class asks for a helper in its **constructor**, Spring **autowires** a matching bean. If two match, I choose with **`@Primary`** or **`@Qualifier`**. Beans are **singletons** (shared) unless I say **prototype**. Each bean gets a **`@PostConstruct`** set-up and a **`@PreDestroy`** clean-up. Settings live in a **`.properties`** file that I read with **`@Value`**. And **profiles** let me swap beans depending on the situation."

---

## The project: School Library

| File | Concept it shows |
|---|---|
| `BookRepository` | `@Repository`, singleton, `@PostConstruct` (loads books at start-up) |
| `ConsoleNotifier` / `SmsNotifier` | Two candidates, `@Primary` |
| `LibraryService` | `@Service`, constructor injection, `@Qualifier`, `@Value` |
| `LibraryConfig` | `@ComponentScan`, `@PropertySource` |
| `LibraryApp` | Starts the container, borrows books, hits the limit |

---

# ✅ Test Yourself

## Part A — Quick check

1. What's the difference between `@Bean` and `@Component`?
2. Name the four "badge" annotations and the role each one describes.
3. Your `@Service` class is in package `com.shop.service`. Your `@ComponentScan` config is in `com.shop.config`. Will Spring find the service? Why or why not?
4. Which type of injection is recommended, and give two reasons why.
5. You see the error `expected single matching bean but found 2`. What happened? Give two ways to fix it.
6. What is the default scope of a Spring bean? What does it mean?
7. You ask Spring for a prototype bean 3 times. How many objects exist?
8. In what order do these happen: `@PreDestroy`, constructor, `@PostConstruct`, injection?
9. What does `@Value("${app.timeout:30}")` do if `app.timeout` is NOT in the properties file?
10. What is the default bean name of a class called `PaymentGateway`?

## Part B — Explain it like a teacher

11. Explain singleton vs prototype using an example that **isn't** the school (e.g. a church, a restaurant, a football club).
12. Why is storing `currentUserName` in a field of a singleton `@Service` a dangerous bug in a web app?

## Part C — Hands-on tasks

13. In `project/library.properties`, change `library.max-books-per-student` to `3`. Run the app. What changed, and what Java did you edit? (Answer: none!)
14. In `LibraryService`, remove `@Qualifier("smsNotifier")`. Which notifier is used now, and why?
15. Remove `@Primary` from `ConsoleNotifier` **and** remove `@Qualifier` from `LibraryService`. Run it, read the error, and explain it in your own words.
16. Add a new `@Component` class `LibraryCleaner` with a `@PreDestroy` method that prints "Floors swept". Where does it appear in the output?

---

<details>
<summary><b>👩‍🏫 Answer key (click to open)</b></summary>

1. `@Bean` goes on a **method** inside a `@Configuration` class, and *you* write the `new` yourself. `@Component` goes on a **class**, and Spring finds it by scanning and creates it itself. Use `@Bean` for classes you can't edit (from libraries).
2. `@Component` (general helper), `@Service` (business logic), `@Repository` (data access), `@Controller` (web requests).
3. **No.** Scanning starts from the config's package (`com.shop.config`) and goes *down*. `com.shop.service` is a sibling, not a child. Fix: move the config to `com.shop`, or use `@ComponentScan("com.shop")`.
4. **Constructor injection.** Fields can be `final`, all dependencies are visible in one place, you can't create the object without them, and it's easy to test with `new X(fakeY)`.
5. Two beans of the same type match one injection point. Fix with `@Primary` on one of them, or `@Qualifier("beanName")` where it's injected.
6. **Singleton**: one shared instance for the whole container.
7. **3** different objects (one per request).
8. Constructor → injection → `@PostConstruct` → (bean used) → `@PreDestroy`.
9. It uses the default value `30`.
10. `paymentGateway`.
11. Example (restaurant): the **menu board on the wall** is a singleton, and every customer reads the same one. The **plate of food** is a prototype, and each customer gets their own.
12. A singleton is **shared by every user at the same time**. User A sets the name, then User B overwrites it, and User A now sees User B's data.
13. Students can borrow 3 books before being refused. No Java was changed, only the properties file.
14. `ConsoleNotifier`, because it's marked `@Primary`, so it is the default when no qualifier is given.
15. `NoUniqueBeanDefinitionException`. Spring found two `Notifier` beans and had no rule for choosing between them.
16. At the very end, when the container closes (after "Done").
</details>
