# Lesson 5 — Spring Security

**Goal of this lesson:** protect an app so that only the right people can do the right things, and handle passwords safely.

---

## The big picture story: "The School Gate"

| Real life | Spring Security |
|---|---|
| The **gate** every visitor must pass | The **security filter chain** |
| A row of **checkpoints** at the gate (bag check, ID check, visitor log) | **Filters**, one after another |
| "**Who are you?** Show me your ID card." | **Authentication** |
| "You're a student. You **can't** enter the staff room." | **Authorization** |
| No ID / fake ID → "**Go away**" | **401 Unauthorized** |
| Real ID, wrong area → "**Not allowed in here**" | **403 Forbidden** |
| The school's **register of ID-card holders** | **`UserDetailsService`** |
| Not storing anyone's real key, only a **fingerprint** of it | **Password hashing (BCrypt)** |
| ID card **colour**: green = student, blue = teacher, red = admin | **Roles** |
| The **rules poster** at the gate: "Staff room: blue and red only" | **`SecurityFilterChain`** config |
| The **visitor badge** you wear once you're inside | **`Authentication`** in the `SecurityContext` |
| An **extra lock** on the principal's own office door | **`@PreAuthorize`** (method security) |

---

## Concept 1 — Why security?

**Plain meaning:** a web app is reachable by **anyone** who knows its address. Without security, every endpoint is open to every person and every robot on the network.

**Real-life illustration:** a school with **no gate and no gate man**. Strangers walk into the exam office, students wander into the staff room. Nobody can say who did what.

**Hold on to:** security must be **in front of everything**, and it must never depend on a developer remembering an `if` in every method.

---

## Concept 2 — Authentication vs Authorization

| | **Authentication** | **Authorization** |
|---|---|---|
| Question | **Who are you?** | **What are you allowed to do?** |
| Gate story | Checking the **ID card** is real | Checking the **colour** of the card for this room |
| Happens | First | Second (only after we know who you are) |
| Failure code | **401 Unauthorized** (we don't know you) | **403 Forbidden** (we know you, but no) |

**Real-life illustration:** at the airport, **showing your passport** is authentication. Your **boarding pass for flight 23** decides whether you may board *that* plane. That's authorization.

**Hold on to:** **AuthN = identity. AuthZ = permission.** 401 vs 403. Know this cold; every interviewer asks it.

---

## Concept 3 — The security filter chain

**Plain meaning:** a **Filter** is code that runs on a request *before* it reaches the `DispatcherServlet` (Topic 4). Spring Security adds a **chain of filters**. Each one does one job (read the login details, check them, check the rules...). If any filter says "no", the request **never reaches your controller**.

```
Request ─▶ [ Filter: read credentials ] ─▶ [ Filter: authenticate ] ─▶ [ Filter: check the rules ] ─▶ DispatcherServlet ─▶ Controller
                                                   │ ✗ 401                     │ ✗ 403
```

**Real-life illustration:** the **checkpoints at the school gate**. Bag check → ID check → visitor log → *then* you reach the receptionist. If the ID check fails, you never even see the receptionist.

👉 Run `example/Example1_FilterChainIdea.java`. It's a plain-Java model of checkpoints so you can *see* the idea.

**Hold on to:** **security runs BEFORE your code**. Your controllers stay clean, with no password checks inside them.

---

## Concept 4 — Secure by default

**Plain meaning:** the moment you add `spring-boot-starter-security`, Spring Boot **locks every endpoint**, creates one user called `user` with a random password printed in the console, and adds a login page. *Locked by default, opened on purpose.*

**Real-life illustration:** a new school building is delivered with **every door locked** and one master key. The principal then decides which doors to open, and for whom.

**Hold on to:** this is the safe choice. Forgetting a rule means "locked", not "open".

> In this course project, `src/main/resources/application.properties` switches security **off** for the other topics (`spring.autoconfigure.exclude=...`) so they stay simple. `application-security.properties` switches it back **on** for this topic's app.

---

## Concept 5 — Users: `UserDetails` and `UserDetailsService`

**Plain meaning:**
- A **`UserDetails`** is Spring's description of one user: **username, (hashed) password, roles**.
- A **`UserDetailsService`** is the object Spring asks: *"give me the user called X"*. You provide it. It can read users from memory, a database, LDAP...

**Real-life illustration:** the **school register of ID-card holders**. When someone shows a card, the gate man **looks the name up in the register** to compare details.

```java
@Bean
UserDetailsService users(PasswordEncoder encoder) {
    UserDetails ada = User.withUsername("ada")
            .password(encoder.encode("ada123"))
            .roles("STUDENT")
            .build();
    return new InMemoryUserDetailsManager(ada /*, more users */);
}
```

**Hold on to:** in real apps you write a `UserDetailsService` that loads users **from your database** (Topic 3/7 skills!). The rest of Spring Security doesn't change.

---

## Concept 6 — Password hashing (`PasswordEncoder`, BCrypt)

**Plain meaning:** **never store real passwords.** Store a **hash**: a scrambled fingerprint made by a **one-way** function. To check a login, hash what the user typed and **compare fingerprints**.

**Real-life illustration:** a **meat grinder**. You can turn meat into mince, but you can **never turn mince back into the original meat**. To check if someone brought the same meat, you grind theirs too and compare the mince.

**BCrypt** (Spring's recommended encoder) adds two protections:
1. **Salt**: a random "spice" mixed in, so the **same password gives a different hash every time**. Attackers can't use pre-computed tables.
2. **Slow on purpose**: makes guessing millions of passwords very expensive.

```java
PasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode("ada123");          // $2a$10$Xy9...  (different every run)
encoder.matches("ada123", hash);                  // true
encoder.matches("wrong", hash);                   // false
```

👉 Run `example/Example2_PasswordHashing.java`

**Hold on to:** if the database leaks, attackers get **mince, not meat**. Encoding ≠ encryption: there is **no "decode"**.

---

## Concept 7 — Roles (authorities)

**Plain meaning:** a **role** is a label that groups permissions: `STUDENT`, `TEACHER`, `ADMIN`. Rules are written for roles, not for individual people.

**Real-life illustration:** the **colour of the ID card**. The staff room rule says "**blue or red cards**", not "Mr. Bello, Mrs. Ade, Mr. Musa...". A new teacher gets a blue card and is automatically allowed in.

**Hold on to:** `.roles("ADMIN")` is stored internally as the authority `ROLE_ADMIN`. `hasRole("ADMIN")` adds the `ROLE_` prefix for you.

---

## Concept 8 — Writing the rules: `SecurityFilterChain`

**Plain meaning:** you write **one bean** that describes the gate rules: which URLs are public, which need login, which need a role.

```java
@Bean
SecurityFilterChain rules(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**").permitAll()                    // anyone
            .requestMatchers("/results/my").hasRole("STUDENT")            // students only
            .requestMatchers(HttpMethod.POST, "/results").hasRole("TEACHER")
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())                                // everything else: logged in
        .httpBasic(Customizer.withDefaults());                            // how to log in (Concept 9)
    return http.build();
}
```

**Real-life illustration:** the **rules poster** at the gate, read **top to bottom**. The first rule that matches your destination decides.

**Hold on to:** **order matters: specific rules first, general rules last.** Always finish with `anyRequest()...` so nothing slips through.

---

## Concept 9 — How a client logs in (HTTP Basic, form login, sessions, CSRF)

| Way | How it works | Good for |
|---|---|---|
| **HTTP Basic** | The client sends `Authorization: Basic base64(username:password)` with **every** request | Simple APIs, learning, tools (we use this) |
| **Form login** | An HTML login page; then a **session cookie** remembers you | Classic websites |
| **Tokens (JWT / OAuth2)** | Log in once, get a **signed token**, and send the token each time | Mobile apps, microservices (Topic 8) |

> ⚠️ Base64 is **not** encryption. Anyone can decode it. Real apps must use **HTTPS**, so the line itself is encrypted.

**CSRF in one paragraph:** with session cookies, a malicious website could trick your browser into sending a request to your bank *with your cookie*. Spring's **CSRF protection** demands a secret token in every form. Our API uses HTTP Basic from tools (not cookies from a browser form), so we switch CSRF off with `.csrf(csrf -> csrf.disable())` to keep testing easy. **Leave it ON for cookie-based websites.**

**Hold on to:** **HTTP Basic = show your ID card at every door.** Form login = get a visitor badge (session) once.

---

## Concept 10 — Who is logged in? (`Authentication`, `@PreAuthorize`)

**Plain meaning:** after login, Spring stores an **`Authentication`** object (username + roles) for the current request. Your controller can simply **ask for it as a parameter**.

```java
@GetMapping("/me")
public Map<String, Object> me(Authentication auth) {
    return Map.of("username", auth.getName(), "roles", auth.getAuthorities());
}
```

For an **extra lock on a specific method**, switch on `@EnableMethodSecurity` and write:

```java
@PreAuthorize("hasRole('ADMIN')")
public void resetAllResults() { ... }
```

**Real-life illustration:** once inside, you wear a **visitor badge** with your name and colour. Any office can read it. The principal's office has an **extra lock** that checks the badge again (`@PreAuthorize`).

**Hold on to:** **never trust a username sent in the request body** for "my results". Use `auth.getName()`, which Spring has verified.

---

## Putting it all together (tell it as one story)

> "Every request passes through Spring Security's **filter chain** before reaching my controllers. First it **authenticates**: it reads the credentials (e.g. **HTTP Basic**), loads the user from my **`UserDetailsService`**, and compares the password using the **BCrypt `PasswordEncoder`**. If that fails → **401**. Then it **authorizes**: it checks my **`SecurityFilterChain` rules**, top to bottom, against the user's **roles**. If the role is wrong → **403**. If allowed, the request reaches my controller, which can read the verified **`Authentication`** to know who's calling."

---

## The project: School Portal (port 8085)

| Method + URL | Who may call it |
|---|---|
| `GET /public/notice-board` | everyone, no login |
| `GET /me` | any logged-in user |
| `GET /results/my` | STUDENT (sees **only their own** results) |
| `POST /results` | TEACHER |
| `GET /admin/users` | ADMIN |
| `DELETE /admin/results` | ADMIN (also protected with `@PreAuthorize`) |

---

# ✅ Test Yourself

## Part A — Quick check

1. Define authentication and authorization in one sentence each.
2. A request has no login details. Which status code comes back: 401 or 403? And for a student calling `/admin/users`?
3. Where does Spring Security run: before or after the `DispatcherServlet`? Why does that matter?
4. What happens the moment you add Spring Security to a Spring Boot app, with no configuration?
5. What is a `UserDetailsService` for?
6. Why do we hash passwords instead of storing them? Can a BCrypt hash be decoded?
7. Why does BCrypt give a different hash for the same password each time? How can `matches()` still work?
8. What's the difference between `.roles("ADMIN")` and the authority `ROLE_ADMIN`?
9. Why does the order of `requestMatchers` rules matter? What should the last rule be?
10. Is HTTP Basic safe over plain `http://`? Why or why not?
11. How does a controller find out the logged-in username?

## Part B — Explain it like a teacher

12. Explain authentication vs authorization using a **concert or a football stadium**.
13. Explain password hashing to a friend who thinks the company "can just look up your password".

## Part C — Hands-on tasks

14. Run the project. Using `school-portal.http`, prove each of these: (a) the notice board works with no login, (b) `/me` without login → 401, (c) `ada` calling `/admin/users` → 403, (d) `admin` calling it → 200.
15. Log in as `mrbello` and add a result for `tunde`. Then log in as `tunde` and fetch `/results/my`.
16. Add a new user `chioma` / `chioma123` with role STUDENT. Test her `/results/my`.
17. Add a rule so that only TEACHERs and ADMINs can call `GET /results/all` (tip: `hasAnyRole`). Add that endpoint to `PortalController`.
18. Read `SchoolPortalSecurityTest` in `src/test/java`. Run it with `mvnw test`. Which test proves the 403 case?

---

<details>
<summary><b>👩‍🏫 Answer key (click to open)</b></summary>

1. **Authentication**: verifying *who* someone is. **Authorization**: deciding *what* that verified person is allowed to do.
2. No login → **401**. Student calling an admin endpoint → **403**.
3. **Before.** So unauthorised requests never reach your code. Controllers don't need any security `if`s, and one forgotten check can't open a hole.
4. Every endpoint requires login. One user `user` is created with a random password printed in the console. Login is enabled.
5. To load a user (username, hashed password, roles) by username, so Spring can check the login.
6. If the database leaks, attackers don't get real passwords (people reuse passwords on other sites!). **No.** Hashing is one-way, and you can only compare.
7. A random **salt** is added each time. The salt is stored inside the hash string itself, so `matches()` takes the salt out, hashes the typed password with it, and compares.
8. `.roles("ADMIN")` is a shortcut that stores the authority `ROLE_ADMIN`. `hasRole("ADMIN")` checks for `ROLE_ADMIN`.
9. Rules are checked top-to-bottom and the **first match wins**. A general rule placed first would hide the specific ones. Finish with `anyRequest().authenticated()` (or `.denyAll()`).
10. **No.** Base64 is easily decoded, so anyone watching the network can read the password. Always use HTTPS.
11. Add an `Authentication` (or `Principal`) parameter to the method and call `getName()`.
12. Stadium: showing your **ticket and ID at the gate** proves who you are (authentication). Your ticket says **"North Stand"**, so you can't enter the VIP box (authorization → 403).
13. "The company only stores a fingerprint of your password made by a one-way grinder. When you log in, they grind what you typed and compare the fingerprints. They genuinely cannot see your password."
14. (a) 200, (b) 401, (c) 403, (d) 200 with the user list.
15. `POST /results` as `mrbello` with `{"student":"tunde","subject":"Maths","score":77}` → 201. Then `GET /results/my` as `tunde` shows it.
16. Add `User.withUsername("chioma").password(encoder.encode("chioma123")).roles("STUDENT").build()` to the `InMemoryUserDetailsManager`.
17. `.requestMatchers("/results/all").hasAnyRole("TEACHER", "ADMIN")`, placed **before** `anyRequest()`. Endpoint: `@GetMapping("/results/all") public Map<String, List<String>> all() { return resultBook.all(); }`
18. `studentCannotOpenAdminArea` (it expects `status().isForbidden()`).
</details>
