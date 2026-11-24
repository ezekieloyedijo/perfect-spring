# Topic 8 — Spring Cloud and Microservices

> Read this file first. Then read [LESSON.md](LESSON.md). Then run `example/`, then the three apps in `project/`.

---

## 1. The problem that existed

Up to Topic 7, each app was **one program** that did everything: courses, students, results, security, all in one jar. That's called a **monolith**, and for small apps it's perfect.

But picture a **huge school** (think of a national exam body, Jumia, or a bank) built as **one giant building** where every department shares the same walls, pipes and electricity:

1. **One problem stops everything.** A burst pipe in the canteen floods the exam hall too. One bug or crash takes down the *whole* app.
2. **You can't grow just one part.** On results day, millions check results, but you must copy the *entire* app to handle it, including parts nobody is using.
3. **Teams get in each other's way.** 200 developers editing one codebase. Every change needs everyone's approval, and one release ships everything.
4. **Stuck with one technology.** Everything must use the same language, the same versions, the same database.

So big companies split their apps into **microservices**: many **small, independent apps**, each doing **one job**, each with its **own data**, talking to each other over the network.

But splitting creates **new problems**:

| New problem | Question |
|---|---|
| Finding each other | "Service A must call Service B. What's B's address today? It moved, and now there are three copies of it!" |
| Calling each other | "Writing HTTP-call code for every service-to-service call is boring and repetitive." |
| Failures spread | "B is slow or down, so A waits forever, its users wait forever, and everything freezes." |
| Many front doors | "The mobile app now has to know the addresses of 20 services?" |
| Scattered settings | "We have 20 services × 3 environments of config files." |

**Spring Cloud** is a set of ready-made tools that solve exactly these distributed-system problems, using the Spring Boot style you already know.

---

## 2. The end goal of this topic

> **"A microservice is a small Spring Boot app with one job and its own data. Services register in a *service registry* (Eureka) and find each other *by name*, not by address. With *OpenFeign* one service calls another through a simple Java interface, and calls are *load-balanced* across copies. A *circuit breaker* (Resilience4j) stops failures from spreading and gives a *fallback* answer. An *API Gateway* is the single front door, and a *Config Server* centralises settings."**

You'll run a small **school system made of 3 apps**: a registry, a Course Catalog service, and an Enrollment service that calls the catalog. Then you'll **kill** the catalog and watch the circuit breaker keep the system alive.

---

## 3. The learning path (one step at a time)

| Step | Learn this | Which raises the question... |
|---|---|---|
| **1** | **Monolith vs microservices.** One big building vs a campus of small buildings. | "If services are separate apps, how do they talk?" |
| **2** | **Service-to-service calls over HTTP.** One service is the *client* of another (`RestClient`). | "Hard-coding `http://localhost:8081` breaks when it moves or has copies. How do we find services?" |
| **3** | **Service discovery (Eureka).** Services register by **name** in a phone directory. | "There are 2 copies of the catalog. Which one do I call?" |
| **4** | **Client-side load balancing.** Spread calls across copies (`lb://` / Spring Cloud LoadBalancer). | "Can calling another service be as easy as calling a Java method?" |
| **5** | **Declarative clients (OpenFeign).** An interface + annotations = a working HTTP client. | "What if the other service is down or slow?" |
| **6** | **Resilience: circuit breaker + fallback (Resilience4j).** Fail fast, answer gracefully. | "How do outside users reach all these services?" |
| **7** | **API Gateway (concept).** One front door that routes to the right service. | "How do we manage settings for 20 services?" |
| **8** | **Config Server, tracing and more (concept).** Central settings, following one request across services. | 🎓 You've finished the course! |

---

## 4. What is inside this folder

```
topic08_cloud/
├── README.md
├── LESSON.md
├── example/
│   ├── Example1_DirectHttpCall.java       (Step 2: one app calls another with a FIXED address)
│   └── Example2_CircuitBreakerIdea.java   (Step 6: plain-Java model of a circuit breaker)
├── project/          (in real life each of these would be its OWN project/repository)
│   ├── registry/      ServiceRegistryApp.java        - Eureka server         (port 8761)
│   ├── catalog/       CatalogServiceApp.java + CatalogController.java          (port 8081)
│   └── enrollment/    EnrollmentServiceApp.java + EnrollmentController.java
│                      + CatalogClient.java (Feign) + CatalogClientFallback.java
│                      + ResilienceConfig.java (circuit breaker rules)          (port 8082)
└── school-cloud.http

src/main/resources/
├── application-registry.properties
├── application-catalog.properties
└── application-enrollment.properties
```

---

## 5. How to build the project, step by step

**The rule:** build the system in the **order it must start**: first the **registry** (the phone directory), then the service that **answers** (catalog), then the service that **calls** (enrollment). Each service is an ordinary Spring Boot app (Topic 7) plus a few Spring Cloud settings and annotations.

### App 1 — the registry (`project/registry/`)

### Step 1 — `application-registry.properties`
**Why first:** nobody can register until the directory exists.
- `server.port=8761`, `spring.application.name=service-registry`.
- `eureka.client.register-with-eureka=false` and `fetch-registry=false`: the directory doesn't list itself.
- Classroom settings so dead services disappear quickly (`enable-self-preservation=false`, eviction timer).

### Step 2 — `ServiceRegistryApp.java`
- `@EnableEurekaServer`: **one annotation** turns this whole app into the phone directory.
- `main()`: starts Boot with the `registry` profile. The dashboard is at http://localhost:8761.

### App 2 — the service that answers (`project/catalog/`)

### Step 3 — `application-catalog.properties`
**Why now:** gives the service its **name** and tells it where the directory is.
- `spring.application.name=catalog-service`: **the name everyone else will use** to find it.
- `eureka.client.service-url.defaultZone=http://localhost:8761/eureka/`: where to register.
- `eureka.instance.lease-renewal-interval-in-seconds=5`: send a heartbeat every 5 seconds.

### Step 4 — `CatalogController.java` (`@RestController`: this service OWNS the course data)
- **Constructor** `CatalogController(@Value("${server.port}") int)`: remembers its own port, so we can see **which copy** answered.
- `all()`: `GET /catalog/courses`, every course.
- `one(code)`: `GET /catalog/courses/{code}`. Returns the course, or **404** if it's unknown.
- `toCourse(code)`: a helper that builds a `CatalogCourse` with `servedByCatalogPort`.
- **Record** `CatalogCourse(code, title, teacher, servedByCatalogPort)`: the JSON "contract" this service offers.

### Step 5 — `CatalogServiceApp.java`
- `main()`: starts Boot with the `catalog` profile. It **registers itself automatically**, with no special annotation needed. Run it twice (port 8091) for load balancing.

### App 3 — the service that calls (`project/enrollment/`)

### Step 6 — `application-enrollment.properties`
- `spring.application.name=enrollment-service`, the registry address, and `registry-fetch-interval-seconds=5` (refresh the directory often).
- `spring.cloud.openfeign.circuitbreaker.enabled=true`: wrap every Feign call in a **circuit breaker**.

### Step 7 — `CatalogCourse.java` (this service's own copy of the contract)
**Why now:** services **don't share Java classes**. Each one only agrees on the JSON shape.
- Record `CatalogCourse(code, title, teacher, servedByCatalogPort)`: matches the catalog's JSON.

### Step 8 — `CatalogClient.java` (`@FeignClient`: the speed-dial button)
**Why now:** describes the catalog's endpoint as a Java interface. Spring writes the HTTP code.
- `name = "catalog-service"`: finds it **by name** in Eureka and load-balances across the copies.
- `dismiss404 = true`: "course not found" comes back as `null` (a normal answer, not a failure).
- `fallback = CatalogClientFallback.class`: Plan B.
- `findCourse(code)`: `GET /catalog/courses/{code}` on whichever catalog copy is chosen.

### Step 9 — `CatalogClientFallback.java` (`@Component`: Plan B)
**Why now:** the Feign client (Step 8) points to it.
- `findCourse(code)`: used when the catalog is down, too slow, or the breaker is OPEN. It returns a special `CATALOG-UNAVAILABLE` course instead of crashing.

### Step 10 — `ResilienceConfig.java` (`@Configuration`: the breaker's rules)
- `circuitBreakerRules()` (`@Bean`): sets a **3 s timeout** and checks the last **4 calls**. If **50%** of them failed, the breaker goes **OPEN**. It rests **10 s**, then makes **1 test call** (HALF-OPEN).

### Step 11 — `EnrollmentController.java` (`@RestController`: uses the other service)
- **Constructor** `EnrollmentController(CatalogClient)`: Spring injects the Feign client like any other bean.
- `enroll(request)`: `POST /enrollments`. It **calls the catalog** with `catalogClient.findCourse(...)`, then:
  - `null` → **400** "No such course";
  - the fallback answer → saves the enrollment as **PENDING** (202);
  - a real answer → saves it as **CONFIRMED** (201), showing which catalog port answered.
- `all()`: `GET /enrollments`, this service's **own** data.
- **Records** `EnrollmentRequest(student, courseCode)` and `Enrollment(...)`: the incoming and outgoing JSON.

### Step 12 — `EnrollmentServiceApp.java`
- `@EnableFeignClients`: finds `CatalogClient` and generates its code.
- `main()`: starts Boot with the `enrollment` profile.

### Step 13 — `school-cloud.http` (try the whole system)
- Requests for a normal enrollment, load balancing, an unknown course, and the catalog being down (fallback).

> 🔁 **The microservices recipe:** **Registry** (`@EnableEurekaServer`) → **answering service** (a name + the registry URL + a normal controller) → **calling service** (a copy of the contract + `@FeignClient` + fallback + circuit-breaker rules + a controller that calls the client + `@EnableFeignClients`). Start them in that same order.

---

## 6. How to run (you need 3 terminals, or 3 VS Code "Run" buttons)

First compile once: `mvnw compile`

```bash
# Terminal 1: the phone directory (start it FIRST)
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic08_cloud.project.registry.ServiceRegistryApp

# Terminal 2: the course catalog service
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic08_cloud.project.catalog.CatalogServiceApp

# Terminal 3: the enrollment service (it calls the catalog)
mvnw spring-boot:run -Dstart-class=com.perfectspring.topic08_cloud.project.enrollment.EnrollmentServiceApp
```

1. Open the **Eureka dashboard**: http://localhost:8761. After about 10–30 seconds you'll see `CATALOG-SERVICE` and `ENROLLMENT-SERVICE` registered.
2. Use [school-cloud.http](school-cloud.http) to enroll students.
   > ⏳ **Got `PENDING` straight after starting everything?** That's normal. The catalog hasn't finished registering yet, so the circuit breaker used the fallback. Wait 15–30 seconds and try again. You'll get `CONFIRMED`. (You've just seen resilience in action!)
3. **Load balancing:** start a **second catalog** on another port, then enroll several times. Watch `servedByCatalogPort` switch between 8081 and 8091:
   ```bash
   mvnw spring-boot:run -Dstart-class=com.perfectspring.topic08_cloud.project.catalog.CatalogServiceApp -Dspring-boot.run.arguments=--server.port=8091
   ```
4. **Resilience:** stop all catalog services (Ctrl + C) and enroll again. You get a `PENDING` enrollment from the **fallback**, not a crash.
