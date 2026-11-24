# Topic 4 — Spring Web MVC Framework

> Read this file first. Then read [LESSON.md](LESSON.md). Then run `example/` in order, then `project/`.

---

## 1. The problem that existed

Topics 1–3 were **console apps**: only the person at the keyboard could use them.
Real apps (a school portal, a banking app, a mobile app's backend) must answer **requests that come over the internet** from browsers and phones.

Java's original tool for this is the **Servlet**: a class that receives a raw HTTP request and writes a raw HTTP response. Writing servlets by hand is painful:

```java
protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String path = req.getRequestURI();
    if (path.equals("/students")) { ... }                 // route requests yourself with if/else
    else if (path.startsWith("/students/")) {
        String idText = path.substring(10);               // cut the ID out of the URL yourself
        long id = Long.parseLong(idText);                 // convert it yourself
        ...
        resp.setContentType("application/json");
        resp.getWriter().write("{\"id\":" + id + ",\"name\":\"" + name + "\"}"); // build JSON by hand 😩
    }
}
```

The problems:
1. **Routing by hand.** Giant `if/else` chains decide which code handles which URL.
2. **Reading input by hand.** Cutting IDs out of URLs, parsing numbers, reading forms.
3. **Writing output by hand.** Building JSON strings character by character.
4. **No structure.** Business logic, database code and HTTP code all mixed in one class.

**Spring Web MVC** gives you a **receptionist** (`DispatcherServlet`) who receives *every* request and sends it to the right method. Annotations replace the `if/else`. Java objects are converted to and from JSON automatically.

---

## 2. The end goal of this topic

> **"I write a `@RestController` class. Each method has a mapping like `@GetMapping("/homework/{id}")`. Spring's `DispatcherServlet` sends each request to the right method, fills in my parameters (`@PathVariable`, `@RequestParam`, `@RequestBody`), and turns my returned objects into JSON. I choose the status code with `ResponseEntity`, keep my code in layers (controller → service → repository), and handle errors in one place with `@RestControllerAdvice`."**

You'll build a **Homework Tracker REST API** that you can call from VS Code, Postman, a browser or curl.

---

## 3. The learning path (one step at a time)

| Step | Learn this | Which raises the question... |
|---|---|---|
| **1** | **Client and server.** A browser or app (client) asks; your program (server) answers. | "What does a 'request' actually contain?" |
| **2** | **HTTP request and response.** Method (GET/POST/PUT/DELETE), URL, headers, body and status code. | "What program receives these requests in Java?" |
| **3** | **Servlets and Tomcat.** Tomcat is the building; a servlet is the person answering. Raw servlets are painful. | "How does Spring make this easier?" |
| **4** | **`DispatcherServlet` (Front Controller).** One receptionist receives every request and routes it. | "How does the receptionist know which method to call?" |
| **5** | **Controllers and mappings.** `@RestController`, `@GetMapping`, `@PostMapping`... | "How do I read data that came with the request?" |
| **6** | **Reading input.** `@PathVariable`, `@RequestParam`, `@RequestBody`. | "How do Java objects become text the client understands?" |
| **7** | **JSON and message converters (Jackson).** Objects ⇄ JSON, automatically. | "How do I say 'created' or 'not found'?" |
| **8** | **`ResponseEntity` and status codes.** 200, 201, 204, 400, 404... | "Where should the business logic go?" |
| **9** | **Layered architecture.** Controller → Service → Repository. | "What happens when something goes wrong?" |
| **10** | **Error handling.** `@ExceptionHandler` + `@RestControllerAdvice`. | Go to Topic 5. |

---

## 4. What is inside this folder

```
topic04_webmvc/
├── README.md
├── LESSON.md
├── server/
│   └── TomcatLauncher.java       (starts Tomcat + Spring's DispatcherServlet by hand)
├── example/
│   ├── Example1_RawServlet.java      (Step 3: the painful way, port 8041)
│   └── Example2_HelloController.java (Steps 4-8: the Spring MVC way, port 8042)
├── project/  "Homework Tracker REST API" (port 8084)
│   ├── Homework.java
│   ├── HomeworkRepository.java          (in-memory storage)
│   ├── HomeworkService.java             (business rules)
│   ├── HomeworkController.java          (HTTP endpoints)
│   ├── HomeworkNotFoundException.java
│   ├── GlobalErrorHandler.java          (@RestControllerAdvice)
│   ├── WebConfig.java                   (@EnableWebMvc + @ComponentScan)
│   └── HomeworkApp.java                 (main)
└── homework-api.http   (ready-made requests to click in VS Code)
```

> 💡 **Why start Tomcat by hand?** So you can **see** the `DispatcherServlet` being created and plugged in. In Topic 7, Spring Boot does all of this for you in one line. You'll appreciate it more after seeing it by hand!

---

## 5. How to build the project, step by step

**The rule:** build the **layers from the bottom up**: data → repository → service → controller → error handling → configuration → server launcher. Then **design your endpoints** (the URL table in LESSON.md) *before* writing the controller.

### Step 1 — `Homework.java` (the data that travels as JSON)
**Why first:** every layer passes this object around, and it's what clients send and receive.
- **Empty constructor** `Homework()`: Jackson needs it to build an object from incoming JSON.
- **Constructor** `Homework(id, subject, description)`: a quick way for us to create one.
- Getters and setters (`getId/setId`, `getSubject/setSubject`, `getDescription/setDescription`, `isDone/setDone`): Jackson uses getters to **write** JSON and setters to **read** JSON.

### Step 2 — `HomeworkRepository.java` (`@Repository`: storage)
**Why now:** it stores `Homework` objects. It's in memory here, so the topic stays focused on the web.
- **Constructor**: adds two starting homework items.
- `save(homework)`: gives a new item an id (if it has none) and stores it.
- `findAll()`: returns every item.
- `findById(id)`: returns one item, or nothing.
- `deleteById(id)`: removes an item and says whether it existed.

### Step 3 — `HomeworkNotFoundException.java` (a named problem)
**Why now:** the service (next step) needs a clear way to say "that id doesn't exist".
- **Constructor** `HomeworkNotFoundException(id)`: builds the message `Homework #id not found`.

### Step 4 — `HomeworkService.java` (`@Service`: the business rules, NO HTTP here)
**Why now:** it uses the repository and the exception. Controllers will call it.
- `findAll(subjectFilter)`: returns all items, filtered by subject if one is given, sorted by id.
- `findOne(id)`: returns one item, or throws `HomeworkNotFoundException`.
- `create(homework)`: **rule**: the subject must not be empty (otherwise it throws `IllegalArgumentException`). It lets the server choose the id, and new homework starts as not done.
- `markDone(id)`: finds the item and sets `done = true`.
- `delete(id)`: deletes the item, or throws `HomeworkNotFoundException`.

### Step 5 — `HomeworkController.java` (`@RestController`: speaks HTTP only)
**Why now:** it's the thin "front office" on top of the service. `@RequestMapping("/homework")` is the base URL.
- `all(subject)`: `GET /homework`. `@RequestParam` reads the optional `?subject=`.
- `one(id)`: `GET /homework/{id}`. `@PathVariable` reads the id from the URL.
- `create(homework)`: `POST /homework`. `@RequestBody` turns JSON into a `Homework`. Replies **201 Created**.
- `markDone(id)`: `PUT /homework/{id}/done`.
- `delete(id)`: `DELETE /homework/{id}`. Replies **204 No Content**.

### Step 6 — `GlobalErrorHandler.java` (`@RestControllerAdvice`: the complaints desk)
**Why now:** the service throws exceptions. This turns them into proper HTTP replies, in one place.
- `notFound(e)` (`@ExceptionHandler(HomeworkNotFoundException.class)`): replies **404** with `{"error": ...}`.
- `badRequest(e)` (`@ExceptionHandler(IllegalArgumentException.class)`): replies **400** with `{"error": ...}`.

### Step 7 — `WebConfig.java` (`@Configuration`: switch Spring MVC on)
**Why now:** all the web classes exist, so tell Spring to switch MVC on and find them.
- `@EnableWebMvc`: turns on request mappings, JSON conversion and so on.
- `@ComponentScan`: finds the controller, service, repository and error handler.
- `@Import(JsonOnlyWebConfig.class)`: makes the app answer in JSON only.

### Step 8 — `server/TomcatLauncher.java` (the server, written once and reused)
**Why now:** something must receive HTTP requests and hand them to Spring.
- `startSpringMvc(port, configClass)`: creates Spring's web container, creates the **`DispatcherServlet`** (the receptionist), and seats it at the front desk.
- `start(port, servlet)`: starts Tomcat on the port, maps **every URL (`/`)** to that servlet, and waits for visitors.

### Step 9 — `HomeworkApp.java` (the launcher)
- `main()`: `TomcatLauncher.startSpringMvc(8084, WebConfig.class)`.

### Step 10 — `homework-api.http` (try every endpoint)
- One ready-made request per endpoint, including the error cases (404, 400).

> 🔁 **The pattern to remember:** Data class → `@Repository` → exceptions → `@Service` → `@RestController` → `@RestControllerAdvice` → `@Configuration` + `@EnableWebMvc` → server + `DispatcherServlet` → `main` → test the requests.

---

## 6. How to run and try it

```bash
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic04_webmvc.example.Example1_RawServlet
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic04_webmvc.example.Example2_HelloController
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic04_webmvc.project.HomeworkApp
```

Then send requests in any of these ways:

1. **Browser** (GET requests only): open http://localhost:8084/homework
2. **VS Code**: install the *REST Client* extension, open [homework-api.http](homework-api.http) and click **"Send Request"** above each request.
3. **Postman**: copy the requests from `homework-api.http`.
4. **curl in Git Bash**:
   ```bash
   curl http://localhost:8084/homework
   curl -X POST http://localhost:8084/homework -H "Content-Type: application/json" -d '{"subject":"Physics","description":"Read chapter 3"}'
   ```
   > ⚠️ In Windows PowerShell, `curl` is a different command. Type `curl.exe`, or use Git Bash.

Stop the server with **Ctrl + C**.
