# Lesson 4 — Spring Web MVC Framework

**Goal of this lesson:** make your Java program answer requests that come over the internet, cleanly and without hand-written plumbing.

---

## The big picture story: "The School's Front Office"

| Real life | Web MVC |
|---|---|
| A **visitor** who comes with a request | The **client** (browser, mobile app, Postman) |
| The **school building** with doors open to visitors | The **web server** (Tomcat) |
| The visitor's **request slip**: *what* they want, *which office*, *attached form* | The **HTTP request**: method, URL, body |
| The **reply letter** with a stamp: "APPROVED", "NOT FOUND"... | The **HTTP response** with a **status code** |
| ONE **receptionist** at the front desk who receives every visitor | **`DispatcherServlet`** (the Front Controller) |
| The **directory board**: "Admissions → Room 4" | **Mappings**: `@GetMapping("/homework")` |
| The **office** that handles a type of request | A **`@RestController`** class |
| The **room number** written on the slip / a **question** at the bottom / an **attached form** | `@PathVariable` / `@RequestParam` / `@RequestBody` |
| A **universal form language** every school understands | **JSON** |
| The **translator** who fills and reads those forms | **Jackson** (message converter) |
| The **complaints desk** that turns problems into polite replies | **`@RestControllerAdvice`** |

---

## Concept 1 — Client and server

**Plain meaning:** a **client** *asks* for something; a **server** *answers*. Your browser is a client. A Spring app running on your PC is a server. They talk over the network.

**Real-life illustration:** a **customer** (client) at a **restaurant counter** (server). The customer asks, the counter answers. The counter never walks up to you first. It **waits for requests**.

**Hold on to:** a server app **keeps running and waits**. That's why the web examples don't finish like Topics 1–3 did. Stop them with **Ctrl + C**.

---

## Concept 2 — The HTTP request and response

**Plain meaning:** **HTTP** is the language clients and servers use. Every request has:

| Part | Meaning | Example |
|---|---|---|
| **Method** | *What* you want to do | `GET` (read), `POST` (create), `PUT` (update), `DELETE` (remove) |
| **URL / path** | *Which* thing | `/homework/3` |
| **Query params** | Extra questions | `?subject=Maths` |
| **Headers** | Envelope info | `Content-Type: application/json` |
| **Body** | Attached data (POST/PUT) | `{"subject":"Maths","description":"Ex 4.2"}` |

Every **response** has a **status code** + headers + a body:

| Code | Meaning | Front-office stamp |
|---|---|---|
| **200** OK | Here you go | ✅ APPROVED |
| **201** Created | New thing made | 🆕 REGISTERED |
| **204** No Content | Done, nothing to show | 👍 DONE |
| **400** Bad Request | Your form is wrong | ✏️ FIX YOUR FORM |
| **401 / 403** | Not logged in / not allowed (Topic 5) | 🚫 NO ENTRY |
| **404** Not Found | Doesn't exist | ❓ NO SUCH OFFICE / FILE |
| **500** Server Error | We messed up | 🔥 OUR FAULT |

**Real-life illustration:** a **request slip** at the front desk: *"ACTION: Show me | OFFICE: Homework | ITEM: 3"*. The reply comes back stamped *APPROVED* or *NOT FOUND*.

**Hold on to:** **method + URL** together say *what* the client wants. **Status code** says *how it went*.

---

## Concept 3 — Servlets and Tomcat (the old, painful way)

**Plain meaning:**
- **Tomcat** is a **servlet container**: a program that listens on a port (e.g. 8084), receives HTTP requests and passes them to Java code.
- A **Servlet** is a Java class that receives a request object and writes a response.

**Real-life illustration:** Tomcat is the **school building with open doors**. A servlet is a staff member at the door who must personally read every slip, work out where it goes, fetch the answer, and **write the reply letter by hand**.

👉 Run `example/Example1_RawServlet.java` and visit http://localhost:8041/hello?name=Ada and http://localhost:8041/students/1. Then read the code: routing, parsing and JSON are all done by hand.

**Hold on to:** servlets *work*, but you end up rewriting the same plumbing in every app.

---

## Concept 4 — `DispatcherServlet`: the Front Controller

**Plain meaning:** Spring MVC registers **one special servlet**, the **`DispatcherServlet`**, that receives **every** request. It looks up which of *your* methods should handle it, calls that method, and sends the result back.

**Real-life illustration:** instead of every staff member standing at the door, the school hires **one professional receptionist**. Every visitor goes to her first. She checks the **directory board** and walks them to the right office. This pattern is called the **Front Controller**.

The journey of one request:
```
Browser ──GET /homework/3──▶ Tomcat ──▶ DispatcherServlet
                                          │ 1. "Who handles GET /homework/{id}?"  (HandlerMapping)
                                          ▼
                              HomeworkController.getOne(3)   ← your code
                                          │ 2. returns a Homework object
                                          ▼
                              Jackson turns it into JSON      (HttpMessageConverter)
                                          │
Browser ◀──200 {"id":3,...}───────────────┘
```

In `server/TomcatLauncher.java` you can see us create the `DispatcherServlet` and map it to `/`, meaning "every URL".

**Hold on to:** **you never call your controller methods yourself.** The `DispatcherServlet` calls them (the Hollywood Principle from Topic 1 again!).

---

## Concept 5 — Controllers and request mappings

**Plain meaning:** a **controller** is a class whose methods handle requests. Annotations form the **directory board**:

```java
@RestController                       // "I answer web requests and my return values become JSON"
@RequestMapping("/homework")          // every URL in this class starts with /homework
public class HomeworkController {

    @GetMapping                       // GET    /homework
    public List<Homework> all() { ... }

    @GetMapping("/{id}")              // GET    /homework/3
    public Homework one(...) { ... }

    @PostMapping                      // POST   /homework
    public ... create(...) { ... }

    @PutMapping("/{id}")              // PUT    /homework/3
    public ... update(...) { ... }

    @DeleteMapping("/{id}")           // DELETE /homework/3
    public ... delete(...) { ... }
}
```

> **`@Controller` vs `@RestController`:** `@Controller` methods usually return the **name of an HTML page** (a *View*, e.g. with Thymeleaf). That's the "V" in **M**odel-**V**iew-**C**ontroller. `@RestController` = `@Controller` + `@ResponseBody`: the return value **is** the response (usually JSON). Modern apps whose frontend is React, Flutter or Android mostly use `@RestController`.

**Hold on to:** **HTTP method + path → one Java method.** That's a mapping.

---

## Concept 6 — Reading input from the request

| Annotation | Reads from | Example URL / body | Code |
|---|---|---|---|
| `@PathVariable` | a **part of the path** | `/homework/3` | `@PathVariable("id") long id` |
| `@RequestParam` | the **`?question`** | `/homework?subject=Maths` | `@RequestParam("subject") String subject` |
| `@RequestBody` | the **JSON body** | `{"subject":"Maths"}` | `@RequestBody Homework homework` |

**Real-life illustration:**
- `@PathVariable` = the **room number** in the address ("Office *Homework*, **file 3**").
- `@RequestParam` = a **question at the bottom of the slip** ("only Maths please").
- `@RequestBody` = the **filled-in form attached** to the slip.

**Hold on to:** path = *which one*. Param = *filter/option*. Body = *the data you're sending*.

---

## Concept 7 — JSON and message converters

**Plain meaning:** **JSON** is a simple text format that every language understands:
```json
{ "id": 3, "subject": "Maths", "description": "Exercise 4.2", "done": false }
```
Spring uses a library called **Jackson** to convert automatically:
- **Java object → JSON** when you `return` it from a controller.
- **JSON → Java object** when a parameter has `@RequestBody`.

**Real-life illustration:** a **translator** at the front desk. Visitors write forms in "JSON language". The translator fills in the Java version for the office and translates the reply back.

**Hold on to:** for Jackson to fill your object, the class needs an **empty constructor + getters/setters** (or be a Java `record`).

---

## Concept 8 — `ResponseEntity` and status codes

**Plain meaning:** returning an object gives **200 OK** by default. When you want a different stamp, return a **`ResponseEntity`**: the full reply **envelope** (status + headers + body).

```java
return ResponseEntity.status(HttpStatus.CREATED).body(saved);   // 201 + the new item
return ResponseEntity.noContent().build();                       // 204, empty body
```

**Hold on to:** **choose honest status codes.** Clients (mobile apps, frontends) rely on them to know what happened.

---

## Concept 9 — Layered architecture

**Plain meaning:** keep each job in its own layer:

```
Controller   (front office: speaks HTTP, no business rules)
    ↓
Service      (the teachers: business rules, e.g. "subject cannot be empty")
    ↓
Repository   (records room: saves and loads data)
```

**Real-life illustration:** the **receptionist** doesn't mark exams and the **records officer** doesn't teach. Each person does one job, so the school runs smoothly, and you can replace one without retraining the others.

**Hold on to:** **controllers should be thin.** If you see `if` statements about business rules in a controller, move them to the service.

---

## Concept 10 — Error handling in one place

**Plain meaning:** when something fails (e.g. homework #99 doesn't exist), throw an exception from the service. A class marked **`@RestControllerAdvice`** catches exceptions from **all** controllers and turns them into proper responses.

```java
@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(HomeworkNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(HomeworkNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
}
```

**Real-life illustration:** a **complaints desk**. Offices don't argue with visitors. They send problems to the complaints desk, which replies politely and consistently.

**Hold on to:** no `try/catch` in every controller method. **One central place** for errors.

---

## Putting it all together (tell it as one story)

> "A **client** sends an **HTTP request** (method + URL + maybe a JSON body) to my **server**. **Tomcat** receives it and hands it to Spring's **`DispatcherServlet`**, the front-desk receptionist. She checks the **mappings** and calls the right method in my **`@RestController`**. Spring fills my parameters from the **path**, the **query** and the **body**. My controller asks the **service**, which uses the **repository**. I return an object or a **`ResponseEntity`**, and **Jackson** turns it into **JSON** with the right **status code**. If anything goes wrong, my **`@RestControllerAdvice`** sends a clean error reply."

---

## The project: Homework Tracker REST API (port 8084)

| Method + URL | What it does | Success status |
|---|---|---|
| `GET /homework` | list all (optional `?subject=Maths`) | 200 |
| `GET /homework/{id}` | get one | 200 (404 if missing) |
| `POST /homework` | create (JSON body) | 201 (400 if subject is empty) |
| `PUT /homework/{id}/done` | mark as done | 200 |
| `DELETE /homework/{id}` | delete | 204 |

---

# ✅ Test Yourself

## Part A — Quick check

1. What is the difference between a client and a server?
2. Match each HTTP method to its meaning: GET, POST, PUT, DELETE.
3. What do status codes 200, 201, 204, 400, 404 and 500 mean?
4. What is Tomcat, and what is a Servlet?
5. What is the `DispatcherServlet`, and why is it called a "Front Controller"?
6. What is the difference between `@Controller` and `@RestController`?
7. For the URL `/homework/7?subject=Maths`, which part is read by `@PathVariable` and which by `@RequestParam`?
8. When do you need `@RequestBody`?
9. What does Jackson do?
10. How do you return a **201 Created** status?
11. Name the three layers and the job of each.
12. What is `@RestControllerAdvice` for?

## Part B — Explain it like a teacher

13. Walk through what happens, step by step, when a browser visits `http://localhost:8084/homework/1`.
14. Using a **hospital** as your example, explain the Front Controller pattern.

## Part C — Hands-on tasks

15. Run the project. Use `homework-api.http` to create a new homework item, then fetch it by its ID.
16. Request a homework ID that doesn't exist. What status code and body come back? Which class produced them?
17. Add an endpoint `GET /homework/pending` that returns only homework that isn't done. (Tip: put the filtering in the service, and declare this mapping so it doesn't clash with `/{id}`.)
18. Add a `PUT /homework/{id}` endpoint that changes the description using a JSON body.
19. Send a POST with an empty `subject`. Which status code comes back? Follow the code path that produced it.

---

<details>
<summary><b>👩‍🏫 Answer key (click to open)</b></summary>

1. A client sends requests (e.g. a browser). A server waits for requests and answers them (e.g. our Spring app).
2. GET = read, POST = create, PUT = update/replace, DELETE = remove.
3. 200 OK, 201 Created, 204 No Content (done, empty body), 400 Bad Request (client sent bad data), 404 Not Found, 500 Internal Server Error (server's fault).
4. Tomcat is a web server / servlet container that listens on a port and passes HTTP requests to Java code. A Servlet is a Java class that handles a request and writes a response.
5. The single servlet in Spring MVC that receives every request and dispatches it to the right controller method. It's the one "front door" all requests pass through.
6. `@Controller` methods normally return view (HTML page) names. `@RestController` methods return data that is written directly into the response, usually as JSON.
7. `7` → `@PathVariable`. `Maths` → `@RequestParam("subject")`.
8. When the client sends data in the request body (usually JSON in POST/PUT) and you want it as a Java object.
9. It converts Java objects to JSON (responses) and JSON to Java objects (request bodies).
10. `return ResponseEntity.status(HttpStatus.CREATED).body(obj);`
11. Controller (handles HTTP), Service (business rules), Repository (stores and loads data).
12. Handling exceptions from all controllers in one central place and turning them into proper HTTP responses.
13. Browser sends `GET /homework/1` → Tomcat on port 8084 receives it → passes it to `DispatcherServlet` → it finds `HomeworkController.getOne` via `@GetMapping("/{id}")` → Spring fills `id = 1` → the controller calls the service → the service calls the repository → a `Homework` object returns → Jackson converts it to JSON → 200 response goes back to the browser.
14. Patients don't wander into random wards. Everyone goes to the **reception/triage desk** first, which sends them to the right department (cardiology, dentistry...). Reception = DispatcherServlet, departments = controllers.
15. POST returns 201 with the new item and its `id`. Then `GET /homework/{thatId}` returns it.
16. 404 with `{"error": "Homework #99 not found"}`. The service throws `HomeworkNotFoundException`, and `GlobalErrorHandler` converts it.
17. In the service: `return repository.findAll().stream().filter(h -> !h.isDone()).toList();`. In the controller: `@GetMapping("/pending")`. Spring prefers the more specific literal path `/pending` over `/{id}`.
18. `@PutMapping("/{id}") public Homework update(@PathVariable("id") long id, @RequestBody Homework changes)`, and the service updates the description.
19. 400. `HomeworkService.create` throws `IllegalArgumentException`, which `GlobalErrorHandler.badRequest` converts.
</details>
