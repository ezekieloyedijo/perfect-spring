# Topic 2 — Spring Core

> Read this file first. Then read [LESSON.md](LESSON.md). Then run `example/` (in number order), then `project/`.

---

## 1. The problem that existed

In Topic 1 we fixed *tight coupling* by giving Spring a **hiring plan** (`@Configuration` + `@Bean`).
It worked. But look at what we had to do:

```java
@Bean public EmailSender emailSender() { return new EmailSender(); }
@Bean public BookRepository bookRepository() { return new BookRepository(); }
@Bean public LibraryService libraryService(BookRepository r, EmailSender e) { return new LibraryService(r, e); }
// ... 300 more of these in a real app 😩
```

That brings new problems:

1. **Typing every bean by hand is slow.** A real app has hundreds of classes. The hiring plan becomes a giant phone book.
2. **What if two workers fit the same job?** If we have both `EmailSender` and `SmsSender`, which one does Spring hand in?
3. **Should everyone share one object, or should each get a fresh one?** One school register is shared by all. But every student needs their *own* exam paper.
4. **Some objects need set-up and clean-up.** A database connection must open when the app starts and close when it stops.
5. **Values like "max books = 3" are hard-coded.** Changing a number shouldn't mean changing Java code.

**Spring Core** is the heart of Spring. It answers all five questions.

---

## 2. The end goal of this topic

By the end you should be able to say:

> **"I put `@Component` (or `@Service` / `@Repository`) on my classes. Spring *scans* my packages, finds them, creates them, and injects them into each other through constructors. When two beans fit, I choose with `@Primary` or `@Qualifier`. I know whether a bean is shared (singleton) or fresh (prototype). I can run code at start-up and shut-down. I read settings from a `.properties` file with `@Value`."**

And you'll build a **School Library** app that uses every one of those ideas.

---

## 3. The learning path (one step at a time)

| Step | Learn this | Which raises the question... |
|---|---|---|
| **1** | **Recap: container + beans.** The container is the HR office. Beans are the staff it manages. | "Must I list every bean by hand?" |
| **2** | **`@Component` and friends (`@Service`, `@Repository`, `@Controller`).** Classes *register themselves* by wearing a badge. | "How does Spring find the badges?" |
| **3** | **Component scanning (`@ComponentScan`).** Spring walks through your packages looking for badges. | "Once found, how do beans get their helpers?" |
| **4** | **Autowiring with constructor injection.** Spring looks at the constructor and passes in matching beans. | "What if TWO beans match?" |
| **5** | **`@Primary` and `@Qualifier`.** Pick the default, or name the exact one you want. | "Is the injected bean shared, or a new copy each time?" |
| **6** | **Bean scopes: singleton vs prototype.** Shared by everyone, or fresh for each request. | "Can a bean do work when it's born or when the app closes?" |
| **7** | **Bean life cycle: `@PostConstruct` and `@PreDestroy`.** Set-up and clean-up hooks. | "Where should settings like 'max books = 3' live?" |
| **8** | **External configuration: `@PropertySource` and `@Value`.** Read values from a `.properties` file. | "Can I swap whole beans for different situations?" |
| **9** | **Profiles (`@Profile`).** Different beans for different environments (dev, test, exam season...). | Go to Topic 3. |

---

## 4. What is inside this folder

```
topic02_core/
├── README.md
├── LESSON.md
├── example/   (each example is in its own sub-folder so their beans don't mix)
│   ├── ex1_componentscan/ComponentScanExample.java   (Steps 2-4)
│   ├── ex2_qualifier/QualifierExample.java           (Step 5)
│   ├── ex3_scopes/ScopeExample.java                  (Step 6)
│   ├── ex4_lifecycle/LifecycleExample.java           (Step 7)
│   ├── ex5_properties/PropertiesExample.java         (Step 8)
│   └── ex6_profiles/ProfileExample.java              (Step 9)
└── project/  "School Library"
    ├── Book.java
    ├── BookRepository.java      (@Repository + @PostConstruct)
    ├── Notifier.java            (interface)
    ├── ConsoleNotifier.java     (@Component @Primary)
    ├── SmsNotifier.java         (@Component("smsNotifier"))
    ├── LibraryService.java      (@Service + @Value + constructor injection)
    ├── LibraryConfig.java       (@Configuration + @ComponentScan + @PropertySource)
    └── LibraryApp.java          (main)

src/main/resources/topic02/
├── school.properties    (used by example 5)
└── library.properties   (used by the project)
```

---

## 5. How to build the project, step by step

**The rule:** build **from the bottom up**: data → settings → storage → helpers → business logic → configuration → launcher. Each file only uses files written before it. Each Spring class gets its **badge** (`@Component`, `@Service`, `@Repository`) as you write it.

### Step 1 — `Book.java` (plain data, NOT a bean)
**Why first:** it's the "thing" everything else talks about. It has no Spring annotations, because books are data, not staff.
- **Constructor** `Book(title, author)`: creates a book. It starts as available.
- `getTitle()`, `getAuthor()`, `isAvailable()`: read the book's details.
- `setAvailable(boolean)`: marks the book as borrowed (`false`) or returned (`true`).
- `toString()`: prints the book nicely, adding `[BORROWED]` when it's out.

### Step 2 — `src/main/resources/topic02/library.properties` (the rule book)
**Why now:** settings are decided *before* code reads them, and they stay out of the Java code.
- `library.name`: the library's name.
- `library.max-books-per-student`: the borrowing limit.

### Step 3 — `BookRepository.java` (`@Repository`: storage)
**Why now:** it stores `Book`s (Step 1). The business logic in Step 6 will need it.
- `stockTheShelves()` (`@PostConstruct`): runs **once, automatically**, right after Spring creates the bean, and fills the shelf with starting books. (Life cycle.)
- `findAll()`: returns every book on the shelf.
- `findByTitle(title)`: finds one book by title, or nothing (`Optional`).

### Step 4 — `Notifier.java` (interface: the job description)
**Why now:** the service will need "someone who can message students", so describe the job before hiring.
- `notify(student, message)`: the promise that any notifier can send a message.

### Step 5 — `ConsoleNotifier.java` and `SmsNotifier.java` (two candidates)
**Why now:** two workers for the same job, so we can practise **choosing between beans**.
- `ConsoleNotifier.notify(...)`: prints the message on screen. It's marked **`@Primary`**, so it's the default choice.
- `SmsNotifier.notify(...)`: sends an "SMS". It has the bean name **`"smsNotifier"`**, so it can be requested by name.

### Step 6 — `LibraryService.java` (`@Service`: the business rules)
**Why now:** it uses everything above: the repository, a notifier and the settings.
- **Constructor**: Spring **autowires** all four parameters:
  - `BookRepository`: matched by type (there's only one).
  - `@Qualifier("smsNotifier") Notifier`: chosen **by name**, because two notifiers exist.
  - `@Value("${library.max-books-per-student}") int` and `@Value("${library.name}") String`: read from the properties file.
- `showCatalogue()`: prints the library name, the limit and every book.
- `borrow(student, title)`: applies the rules (limit reached? book exists? already borrowed?), then lends the book and notifies the student.
- `giveBack(student, title)`: makes the book available again, lowers the student's count and notifies them.

### Step 7 — `LibraryConfig.java` (`@Configuration`: tell Spring where to look)
**Why now:** all the badge-wearing classes exist, so tell HR to go and find them.
- `@ComponentScan`: "scan this package and register every class with a badge".
- `@PropertySource("classpath:topic02/library.properties")`: "load this file so `@Value` can read it".
- (No methods! Compare with Topic 1's `AppConfig`, which needed a `@Bean` method for every object.)

### Step 8 — `LibraryApp.java` (the launcher)
**Why last:** it only starts things.
- `main()`: starts the container with `LibraryConfig`, gets the `LibraryService`, and acts out a story (borrow, hit the limit, return, borrow again).

> 🔁 **The pattern to remember:** Data class → `.properties` → `@Repository` → interface + `@Component`s → `@Service` (constructor injection, `@Qualifier`, `@Value`) → `@Configuration` + `@ComponentScan` + `@PropertySource` → `main`.

---

## 6. How to run

In VS Code, click **Run** above any `main`. Or from the terminal:

```bash
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic02_core.example.ex1_componentscan.ComponentScanExample
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic02_core.project.LibraryApp
```
