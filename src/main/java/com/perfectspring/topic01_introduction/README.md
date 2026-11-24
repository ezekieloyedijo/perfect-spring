# Topic 1 — Introduction to the Spring Framework

> Read this file first. Then read [LESSON.md](LESSON.md). Then run the code in `example/`, then `project/`.

---

## 1. The problem that existed

Picture a Java program as a **school**.

- Every **class** in your code is like a **classroom**.
- Every classroom needs **teachers** (other objects) to work. A `Car` needs an `Engine`. An `OrderService` needs a `PaymentService`.

Before Spring, every classroom went out and **hired its own teacher**. In Java this looks like:

```java
public class Car {
    private PetrolEngine engine = new PetrolEngine();   // the Car hires its own engine
}
```

That one `new` keyword causes three big problems:

| Problem | In the school story | In code |
|---|---|---|
| **Tight coupling** | The classroom only accepts *one specific* teacher, Mr. Petrol. | `Car` can only ever have a `PetrolEngine`. |
| **Hard to change** | To get a new teacher, you must rebuild the classroom. | To use an `ElectricEngine` you must edit `Car`'s code. |
| **Hard to test** | You can't bring in a substitute teacher for an exam rehearsal. | You can't give `Car` a fake engine in a test. |

Now multiply this by **hundreds of classes** in a real company app. Everyone is creating everyone with `new`. Changing one thing breaks ten others. Big Java apps of the early 2000s (built with a heavy technology called EJB) became slow and painful to build.

In 2002–2004, Rod Johnson wrote a book and released code that said: *"Stop letting classes hire their own helpers. Let one central office do the hiring and assigning."* That code became the **Spring Framework**.

---

## 2. The end goal of this topic

By the end you should be able to say, in your own words:

> **"Spring is a big helper that creates my objects for me and connects them together. That way my classes stay small, swappable and easy to test. My job is to write the classes and tell Spring how they connect."**

And you should be able to **build a tiny Spring app** (the `project/` folder) where you can switch behaviour by changing **one line of configuration**, without touching the business code.

---

## 3. The learning path (one step at a time)

Each step answers a question that the step before it raises.

| Step | Learn this | Which raises the question... |
|---|---|---|
| **1** | **What a framework is.** A library is a tool *you* call. A framework is a structure that *calls you*. | "OK, so what does Spring do for me?" |
| **2** | **Dependencies.** Objects need other objects to work. `Car` *depends on* `Engine`. | "How should a class get the things it depends on?" |
| **3** | **Tight coupling (the problem).** Using `new` inside a class welds it to one exact helper. | "How do we stop the welding?" |
| **4** | **Interfaces (loose coupling).** Depend on a *shape* (`Engine`), not a *specific thing* (`PetrolEngine`). | "But somebody still has to create the real engine and hand it over. Who?" |
| **5** | **Dependency Injection (DI).** Hand the helper to the class from the outside, through the constructor. | "If classes don't create their helpers, who creates everything?" |
| **6** | **Inversion of Control (IoC).** Control is flipped. A central "boss" now creates and connects objects. | "What is this boss called in Spring?" |
| **7** | **The IoC Container (`ApplicationContext`) and Beans.** The container is the boss. The objects it manages are called *beans*. | "How does the container know *what* to create?" |
| **8** | **Configuration (`@Configuration` + `@Bean`).** We write the instructions the container follows. | "What else can Spring do beyond creating objects?" |
| **9** | **The Spring family map.** Core, Data, Web, Security, Test, Boot, Cloud = topics 2–8 of this course. | Go to Topic 2. |

---

## 4. What is inside this folder

```
topic01_introduction/
├── README.md        <- you are here (the problem + the goal + the path)
├── LESSON.md        <- the full lesson with real-life illustrations + tests
├── example/
│   ├── Example1_TightCoupling.java            (Step 3: the problem)
│   ├── Example2_DependencyInjectionByHand.java (Steps 4-5: the fix, done by hand)
│   └── Example3_SpringDoesTheWiring.java       (Steps 6-8: let Spring do it)
└── project/  "School Announcement System"
    ├── MessageSender.java       (the interface: the "shape")
    ├── EmailSender.java         (one real implementation)
    ├── SmsSender.java           (another real implementation)
    ├── AnnouncementService.java (the business logic: it depends on a MessageSender)
    ├── AppConfig.java           (the instructions for the Spring container)
    └── SchoolAnnouncementApp.java (main: starts the container)
```

---

## 5. How to build the project, step by step

**The rule Spring wants you to follow:** first describe the **job** (interface), then write the **workers** who can do it, then the **class that needs a worker**, then the **hiring plan** (configuration), and last the **launcher** (`main`). Each file only uses files written before it.

### Step 1 — `MessageSender.java` (interface)
**Why first:** before hiring anyone, write the job description. Everything else depends on this shape.
- `send(to, message)`: the promise that "anyone who takes this job can send a message to someone".

### Step 2 — `EmailSender.java` and `SmsSender.java` (the workers)
**Why now:** now that the job exists, create the people who can do it. Each one `implements MessageSender`.
- `send(to, message)` in `EmailSender`: delivers the message as an "email".
- `send(to, message)` in `SmsSender`: delivers the message as an "SMS".

### Step 3 — `AnnouncementService.java` (the class that needs a worker)
**Why now:** it depends on the *job* (`MessageSender`), never on a specific worker, so the interface had to exist first.
- **Constructor** `AnnouncementService(MessageSender)`: *asks* for a sender. This is **constructor injection**. There is no `new EmailSender()` inside.
- `announce(students, message)`: the business logic. It loops over the students and asks the sender to deliver the message to each one.

### Step 4 — `AppConfig.java` (the hiring plan for Spring)
**Why now:** you can only write the plan once all the pieces exist. This is where *Spring's rules* (`@Configuration`, `@Bean`) meet *your* classes.
- `@Configuration` on the class: tells Spring "this class is a hiring plan".
- `messageSender()` (`@Bean`): decides **which worker** gets the job. This is the *one line* you change to switch Email ↔ SMS.
- `announcementService(MessageSender)` (`@Bean`): creates the service. Spring passes in the bean from `messageSender()` automatically.

### Step 5 — `SchoolAnnouncementApp.java` (the launcher)
**Why last:** it just switches everything on.
- `main()`: opens the container with `new AnnotationConfigApplicationContext(AppConfig.class)`, asks it for the ready-made `AnnouncementService` with `getBean(...)`, and calls `announce(...)`.

> 🔁 **The pattern to remember:** Interface → Implementations → Class that uses the interface → `@Configuration` with `@Bean`s → `main` that starts the container.

---

## 6. How to run

**In VS Code:** open any file that has a `main` method and click **Run** above `main`.

**From the terminal** (in the project root folder):

```bash
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic01_introduction.example.Example1_TightCoupling
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic01_introduction.example.Example2_DependencyInjectionByHand
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic01_introduction.example.Example3_SpringDoesTheWiring
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic01_introduction.project.SchoolAnnouncementApp
```

> On macOS/Linux type `./mvnw` instead of `mvnw`.
