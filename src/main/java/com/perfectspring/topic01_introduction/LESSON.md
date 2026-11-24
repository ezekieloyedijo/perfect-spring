# Lesson 1 — Introduction to the Spring Framework

**Goal of this lesson:** understand *why* Spring exists and its single most important idea.
That idea is: **"don't create your helpers yourself; let Spring create and hand them to you."**

---

## The big picture story: "The School and its HR Office"

Keep this one picture in your head for the whole lesson:

| Real life (a school) | Java + Spring |
|---|---|
| A **classroom** | A Java **class** (e.g. `AnnouncementService`) |
| A **teacher** the classroom needs | A **dependency** (e.g. `MessageSender`) |
| A **job description** ("we need *a* Maths teacher") | An **interface** (`MessageSender`) |
| An actual hired **person** (Mrs. Email) | A **class that implements the interface** (`EmailSender`) |
| The **HR office** that hires staff and assigns them | The **Spring IoC Container** (`ApplicationContext`) |
| The **staff list** HR keeps | The **beans** inside the container |
| The **hiring plan** the principal writes for HR | The **configuration** (`@Configuration` class with `@Bean` methods) |

> 🔑 Just like in your Codemagic illustration: **Spring (HR) gives the rules** for how a hiring plan must be written (annotations like `@Bean`). **You, the developer, write the hiring plan** following those rules. Then **Spring follows your plan** to build and connect your objects.

---

## Concept 1 — What is a framework?

**Plain meaning:** a framework is ready-made structure that you fill in with your own code. The framework decides *when* your code runs.

**Real-life illustration:**
- A **library** is like a **toolbox**. *You* pick up the hammer when *you* want. (You call the library.)
- A **framework** is like the **steel frame of a building**. The frame is already standing. You add walls, doors and paint in the places the frame allows. (The framework calls your code.)

People sum this up as the **"Hollywood Principle": *"Don't call us, we'll call you."***

**Hold on to:** with Spring, you write classes, and **Spring decides when to create them and how to connect them**.

---

## Concept 2 — Dependencies

**Plain meaning:** if class A *needs* class B to do its job, then **B is a dependency of A**.

**Real-life illustration:** a **classroom depends on a teacher**. No teacher, no lesson. A **car depends on an engine**. No engine, no movement.

```java
public class Car {
    private Engine engine;        // Car DEPENDS ON Engine
    public void drive() { engine.start(); }
}
```

**Hold on to:** almost every useful class depends on other classes. The question is: **how does it get them?**

---

## Concept 3 — Tight coupling (the problem)

**Plain meaning:** a class is *tightly coupled* when it creates its own dependency with `new`. That welds it to one specific class forever.

**Real-life illustration:** imagine a phone with the **charger cable welded onto it**. If the cable breaks, or you want a faster charger, you have to **open up the phone**. That is tight coupling.

```java
public class Car {
    private PetrolEngine engine = new PetrolEngine(); // welded!
}
```

To switch to an `ElectricEngine`, you must **edit the `Car` class**. In a big app this happens in hundreds of places.

👉 Run `example/Example1_TightCoupling.java`

**Hold on to:** `new SomeHelper()` inside a class = welding. Welding is hard to change and hard to test.

---

## Concept 4 — Interfaces (loose coupling)

**Plain meaning:** instead of depending on a *specific* class, depend on an **interface**. An interface is a promise that says *"anything that can do these jobs is welcome."*

**Real-life illustration:** a modern phone has a **USB-C port**. The port is the interface. *Any* USB-C charger fits: Samsung, Oraimo, a power bank. The phone doesn't care **who** made the charger, only that it **fits the shape**.

```java
public interface Engine { void start(); }            // the "port" (shape)
public class PetrolEngine   implements Engine { ... } // charger #1
public class ElectricEngine implements Engine { ... } // charger #2

public class Car {
    private final Engine engine;   // Car only knows the SHAPE
}
```

**Hold on to:** depend on **what something can do** (interface), not on **who it is** (concrete class).

---

## Concept 5 — Dependency Injection (DI)

**Plain meaning:** *injection* = **handing in** from the outside. The class does **not** create its helper. Someone **gives** it the helper, usually through the **constructor**.

**Real-life illustration:** a **footballer doesn't make their own boots**. The **kit manager hands them boots** before the match. On a rainy day the kit manager hands them boots with longer studs. The footballer's skills (the code) stay the same.

```java
public class Car {
    private final Engine engine;
    public Car(Engine engine) {   // "Please HAND me an engine"
        this.engine = engine;
    }
}

// Somewhere outside, the "kit manager" decides:
Car car = new Car(new ElectricEngine());
```

👉 Run `example/Example2_DependencyInjectionByHand.java`

**Hold on to:** **DI = the class asks for its helpers in its constructor, and someone outside provides them.** Here *we* did the injecting by hand in `main`.

---

## Concept 6 — Inversion of Control (IoC)

**Plain meaning:** normally *your code* is in control of creating objects. With IoC that control is **inverted (flipped)**. A central "boss" creates the objects and connects them, and your classes just receive them.

**Real-life illustration:**
- **Without IoC:** every classroom goes out on the street to find and hire its own teacher. Chaos.
- **With IoC:** the school has an **HR office**. Classrooms just say "I need a Maths teacher". HR hires, keeps the staff list, and **assigns** the teacher to the classroom.

The *control* of hiring moved **from the classroom to HR**. That move is the "inversion".

> **IoC vs DI, in one line each:**
> - **IoC** is the *idea*: "someone else is in control of creating my objects."
> - **DI** is the *technique* that idea uses: "they hand them to me through my constructor."

**Hold on to:** in Example 2 *we* were the HR office in `main`. In a big app that `main` would become huge. So let's hire a professional HR office: **Spring**.

---

## Concept 7 — The IoC Container and Beans

**Plain meaning:**
- The **IoC Container** is Spring's "HR office". In code it is an object called **`ApplicationContext`**.
- A **bean** is simply **any object that the Spring container created and manages**. That's all "bean" means.

**Real-life illustration:** the HR office keeps a **staff list**. Every person on that list is a *bean*. When a classroom needs a teacher, HR looks at the list and assigns one.

```java
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class); // open the HR office
Car car = context.getBean(Car.class);   // "HR, give me the Car, fully assembled"
car.drive();
```

**Hold on to:** **Container = the factory/HR office. Bean = an object the container made.**

---

## Concept 8 — Configuration: telling the container what to build

**Plain meaning:** the container can't read your mind. You give it **instructions**. In this lesson we use a Java class marked **`@Configuration`**, with methods marked **`@Bean`**. Each `@Bean` method says: *"here is how to create this object."*

**Real-life illustration:** this is the **hiring plan** the principal gives HR:
"Hire one Electric Engine. Hire one Car, and give the Car that engine."

```java
@Configuration                 // "This class is a hiring plan"
public class AppConfig {

    @Bean                      // "Hire this"
    public Engine engine() {
        return new ElectricEngine();
    }

    @Bean                      // "Hire this too, and give it the engine above"
    public Car car(Engine engine) {
        return new Car(engine);
    }
}
```

Notice the `car(Engine engine)` method asks for an `Engine` as a parameter. **Spring sees this and passes in the engine bean automatically.** That's Spring doing the injection for you.

👉 Run `example/Example3_SpringDoesTheWiring.java`

**Hold on to:** to switch from Electric to Petrol you change **one line in the configuration**, not the `Car` class.

---

## Concept 9 — The Spring family (map of this course)

Spring grew from that one idea into a whole **family of projects**, all built on the same container:

| Topic | Spring piece | One-line job (school story) |
|---|---|---|
| 2 | **Spring Core** | The HR office in full detail: scanning for staff, scopes, life cycle |
| 3 | **Spring Data Access** | The school's **records room** (saving and reading data in a database) |
| 4 | **Spring Web MVC** | The school's **reception desk** (answering requests from the internet) |
| 5 | **Spring Security** | The **gate man and ID cards** (who can enter, who can go where) |
| 6 | **Spring Testing** | **Mock exams** (checking the code works before real users arrive) |
| 7 | **Spring Boot** | A **ready-furnished school building** (sensible defaults, starts in seconds) |
| 8 | **Spring Cloud** | A **group of schools** working together across a city (microservices) |

---

## Putting it all together (tell it as one story)

> "My classes need helpers (**dependencies**). If each class creates its own with `new`, they get welded together (**tight coupling**). So my classes depend on **interfaces** instead, and they ask for helpers in their constructor (**Dependency Injection**). Someone still has to create everything. So I flip control (**IoC**) and give that job to Spring's **container**. The container follows my **configuration** (`@Configuration`/`@Bean`), creates every object (**beans**), and plugs them together. Now I can change behaviour by changing one line of configuration."

---

## The project: School Announcement System

**What it does:** the school sends an announcement to students.
**The trick:** `AnnouncementService` doesn't know *how* the message travels. It only knows the `MessageSender` interface. `AppConfig` decides whether it's **Email** or **SMS**.

Read the files in this order:
1. `MessageSender.java`: the shape
2. `EmailSender.java` / `SmsSender.java`: two real workers
3. `AnnouncementService.java`: the business logic (asks for a `MessageSender` in its constructor)
4. `AppConfig.java`: the hiring plan
5. `SchoolAnnouncementApp.java`: opens the container and runs

---

# ✅ Test Yourself

## Part A — Quick check (choose or answer briefly)

1. In one sentence, what is a **bean**?
2. Which one is the *idea* and which one is the *technique*: IoC or DI?
3. A class contains `private Printer printer = new LaserPrinter();`. Name the problem and explain why it's bad.
4. True or False: with DI, a class creates its own dependencies.
5. What does `@Configuration` tell Spring? What does `@Bean` tell Spring?
6. What is the difference between a **library** and a **framework**? Use a real-life example.
7. What Java type is Spring's container in our examples?
8. Why is it better for `Car` to depend on `Engine` (interface) than on `PetrolEngine`?

## Part B — Explain it like a teacher

9. Explain IoC to a 12-year-old using **anything except** the school example (e.g. a restaurant, a football club, a wedding planner).
10. In the USB-C charger illustration, what plays the role of (a) the interface, (b) the implementation, (c) the person doing the injection?

## Part C — Hands-on tasks

11. In `project/AppConfig.java`, switch the app from Email to SMS. Run it. Which file(s) did you change? Which did you **not** change?
12. Create a new class `WhatsAppSender implements MessageSender` in the `project` package. Make the app use it. You should not touch `AnnouncementService`.
13. In `Example3_SpringDoesTheWiring.java`, call `context.getBean(Car.class)` **twice** and compare the two objects with `==`. Are they the same object? (Keep the answer: it's the opening question of Topic 2.)

---

<details>
<summary><b>👩‍🏫 Answer key (click to open)</b></summary>

1. A bean is an object that the Spring container creates and manages.
2. **IoC** is the idea (control of creating objects is given to someone else). **DI** is the technique (dependencies are handed in, e.g. through the constructor).
3. **Tight coupling.** The class is welded to `LaserPrinter`. You can't switch to an `InkjetPrinter` or a fake printer for tests without editing the class.
4. **False.** With DI, dependencies are given to the class from outside.
5. `@Configuration`: "this class contains instructions (a hiring plan) for the container." `@Bean`: "the object returned by this method should be created and managed by the container."
6. A library is a toolbox you call when you want (e.g. a calculator app you open). A framework is a structure that calls your code at the right time (e.g. a building frame you fill in, or a school timetable that decides when *your* class teaches).
7. `ApplicationContext`. We create it with `new AnnotationConfigApplicationContext(AppConfig.class)`.
8. Because any engine that implements `Engine` can be plugged in (petrol, electric, a fake test engine) without changing `Car`. That is loose coupling.
9. Example (restaurant): "You don't go into the kitchen and cook your own food. You tell the waiter what you want and the kitchen makes it and brings it to you. The kitchen is in control of cooking, not you."
10. (a) The USB-C port shape. (b) A specific charger, like a Samsung charger. (c) The person who plugs the charger in, i.e. the Spring container.
11. Only `AppConfig.java` changes. `AnnouncementService.java`, `EmailSender.java` and `SmsSender.java` are untouched. That's the whole point.
12. Create `WhatsAppSender` and return `new WhatsAppSender()` from the `messageSender()` `@Bean` method in `AppConfig`.
13. **Yes, `==` is `true`.** By default Spring creates **one** shared bean and gives everyone the same one. This is called the **singleton scope**, and Topic 2 explains it.
</details>
