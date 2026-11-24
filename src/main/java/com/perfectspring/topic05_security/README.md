# Topic 5 — Spring Security

> Read this file first. Then read [LESSON.md](LESSON.md). Then run `example/`, then `project/`.

---

## 1. The problem that existed

In Topic 4 we built a Homework API. **Anyone** on the network could call `DELETE /homework/1`. There was no gate, no ID check, no rules. Imagine a school where:

- **any stranger** can walk into the exam office and change scores,
- a **student** can enter the staff room and read the teachers' files,
- the **list of passwords** is pinned on the notice board in plain text.

Developers used to write their own security code: `if (password.equals(...))` checks copied into every controller, passwords saved as plain text, and session handling done by hand. That code was **full of holes**. One forgotten `if` in one endpoint and the whole system was open. And security bugs are not small bugs: they make the news ("Millions of passwords leaked").

**Spring Security** is a battle-tested **security gate** that sits in front of your whole app. It checks *who you are* and *what you're allowed to do* **before** any of your code runs, and it handles passwords safely.

---

## 2. The end goal of this topic

> **"Spring Security sits in front of my app as a chain of filters. I define my users (`UserDetailsService`), store their passwords as BCrypt hashes (`PasswordEncoder`), give them roles, and write the access rules in one `SecurityFilterChain`. Unknown visitors get 401, known-but-not-allowed users get 403, and inside my controller I can ask who is logged in."**

You'll build a **School Portal** where **students** see their results, **teachers** record scores, and only the **admin** can see the user list.

> 💡 From this topic on we let **Spring Boot** start the server (`SpringApplication.run`) instead of starting Tomcat by hand. For now, treat it as **"the button that starts everything"**. Topic 7 explains exactly what it does.

---

## 3. The learning path (one step at a time)

| Step | Learn this | Which raises the question... |
|---|---|---|
| **1** | **Why security?** Without a gate, anyone can do anything. | "What exactly must the gate check?" |
| **2** | **Authentication vs Authorization.** *Who are you?* (401) vs *Are you allowed?* (403) | "Where does Spring do these checks?" |
| **3** | **The security filter chain.** Checkpoints that run *before* the `DispatcherServlet`. | "What happens if I just add Spring Security?" |
| **4** | **Secure by default.** Adding the library locks *everything*, with one generated user. | "How do I create my own users?" |
| **5** | **Users: `UserDetails` and `UserDetailsService`.** Spring's register of ID-card holders. | "How should their passwords be stored?" |
| **6** | **Password hashing: `PasswordEncoder` / BCrypt.** Store a fingerprint, never the password. | "How do I say what each user may do?" |
| **7** | **Roles (authorities).** The colour of the ID card: STUDENT, TEACHER, ADMIN. | "Where do I write the rules?" |
| **8** | **The rules: `SecurityFilterChain` + `authorizeHttpRequests`.** URL → who may enter. | "How does a client actually send its username and password?" |
| **9** | **Logging in: HTTP Basic (and form login, sessions, CSRF).** | "Inside my code, how do I know who's calling?" |
| **10** | **The logged-in user (`Authentication`, `@PreAuthorize`).** Read the visitor badge; extra checks on methods. | Go to Topic 6. |

---

## 4. What is inside this folder

```
topic05_security/
├── README.md
├── LESSON.md
├── example/
│   ├── Example1_FilterChainIdea.java     (Step 3: a plain-Java model of checkpoints)
│   └── Example2_PasswordHashing.java     (Step 6: BCrypt in action)
├── project/  "School Portal" (port 8085)
│   ├── SchoolPortalApp.java       (main: starts Spring Boot with the "security" profile)
│   ├── SecurityConfig.java        (users, password encoder, the RULES)
│   ├── PortalController.java      (public, /me, student, teacher and admin endpoints)
│   └── ResultBook.java            (in-memory results storage)
└── school-portal.http             (requests with and without logins)

src/main/resources/application-security.properties   (port 8085)
src/test/java/.../topic05_security/project/SchoolPortalSecurityTest.java  (proves the rules work)
```

---

## 5. How to build the project, step by step

**The rule:** first build the app **as if there were no security** (data + controller). Then add the **gate** in one place (`SecurityConfig`), in this order: password encoder → users → rules. Finally, **prove the rules with tests**.

### Step 1 — `application-security.properties` (settings)
**Why first:** decides the port and switches Spring Security's auto-configuration **back on** for this app.
- `server.port=8085`.
- `spring.autoconfigure.exclude=` (empty): undoes the course-wide "security off" setting.

### Step 2 — `ResultBook.java` (`@Service`: the data)
**Why now:** the controller needs something to show and change.
- **Constructor**: adds starting results for `ada` and `tunde`.
- `add(student, subject, score)`: records a result.
- `resultsOf(student)`: returns one student's results.
- `all()`: returns every student's results.
- `clearAll()` (`@PreAuthorize("hasRole('ADMIN')")`): wipes everything. It has an **extra lock**: only an ADMIN may run it, even if a URL rule were forgotten.

### Step 3 — `PortalController.java` (`@RestController`: the endpoints, with NO security code inside)
**Why now:** write the endpoints first. The gate (next step) decides who may reach each one.
- `noticeBoard()`: `GET /public/notice-board`, for everyone.
- `me(Authentication)`: `GET /me`. Spring hands in the **verified** logged-in user, and this returns the username and roles.
- `myResults(Authentication)`: `GET /results/my`. It uses `auth.getName()`, so a student only ever sees **their own** results.
- `addResult(NewResult, Authentication)`: `POST /results`. A teacher records a score. Replies **201** and shows who recorded it.
- `users()`: `GET /admin/users`, the user list.
- `clearResults()`: `DELETE /admin/results`. Calls `ResultBook.clearAll()`.
- **Record** `NewResult(student, subject, score)`: the shape of the JSON a teacher sends.

### Step 4 — `SecurityConfig.java` (`@Configuration`: THE GATE)
**Why now:** with the endpoints known, write the gate rules, in this order:
- `passwordEncoder()` (`@Bean`): **first**, the BCrypt "meat grinder", because users can't be created without it.
- `users(PasswordEncoder)` (`@Bean`): **second**, the register of ID-card holders. Four users with **hashed** passwords and a role each (`UserDetailsService`).
- `rules(HttpSecurity)` (`@Bean`): **third**, the `SecurityFilterChain`, the rules poster read top to bottom:
  - `/public/**` → `permitAll()`
  - `GET /results/my` → `hasRole("STUDENT")`
  - `POST /results` → `hasRole("TEACHER")`
  - `/admin/**` → `hasRole("ADMIN")`
  - everything else → `authenticated()`
  - `httpBasic(...)`: *how* users log in.
  - `csrf(...disable())`: OK for this tool-driven API.
- `@EnableMethodSecurity` on the class: switches on `@PreAuthorize` (used in Step 2).

### Step 5 — `SchoolPortalApp.java` (the launcher)
- `main()`: starts Spring Boot with the `security` profile (which loads Step 1's file).

### Step 6 — `school-portal.http` (try it by hand)
- Requests with **no login**, a **wrong password**, the **wrong role** and the **right role**, so you see 401, 403 and 200 yourself.

### Step 7 — `src/test/.../SchoolPortalSecurityTest.java` (prove it automatically)
- One test per rule, for example `strangerIsAskedWhoTheyAre` (401), `studentCannotOpenAdminArea` (403) and `adminCanOpenAdminArea` (200).

> 🔁 **The pattern to remember:** Settings → data → controller (no security code) → `SecurityConfig`: **PasswordEncoder → UserDetailsService → SecurityFilterChain** → `main` → try 401/403/200 → tests.

---

## 6. How to run

```bash
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic05_security.example.Example1_FilterChainIdea
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic05_security.example.Example2_PasswordHashing
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic05_security.project.SchoolPortalApp
```

**Users in the project** (username / password → role):

| User | Password | Role |
|---|---|---|
| `ada` | `ada123` | STUDENT |
| `tunde` | `tunde123` | STUDENT |
| `mrbello` | `teach123` | TEACHER |
| `admin` | `admin123` | ADMIN |

Try it in a browser: http://localhost:8085/public/notice-board (open to everyone), then http://localhost:8085/me (the browser will ask you to log in).
Use [school-portal.http](school-portal.http) to test every role.
