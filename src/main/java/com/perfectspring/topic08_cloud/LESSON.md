# Lesson 8 — Spring Cloud and Microservices

**Goal of this lesson:** understand why big systems are split into small services, the new problems that splitting creates, and the Spring Cloud tool that solves each one.

---

## The big picture story: "From One Big School to a Campus"

| Real life | Microservices + Spring Cloud |
|---|---|
| **One giant building** doing everything | A **monolith** |
| A **campus** of separate buildings: Admissions, Exam Centre, Canteen | **Microservices** |
| Each building has its **own staff and own filing cabinet** | Each service has its **own code and own database** |
| Buildings **phone each other** | Services call each other over **HTTP** |
| The **campus phone directory**: look up "Exam Centre" by **name**, not by a number you memorised | **Service registry / discovery** (Eureka) |
| Two canteen branches. The switchboard **spreads the calls** between them | **Load balancing** |
| A **speed-dial button** labelled "Exam Centre" | **OpenFeign** client |
| The **electrical breaker** in a house trips to stop a fire spreading | **Circuit breaker** |
| "Canteen is closed, here's a sandwich from the vending machine" | **Fallback** |
| The **main campus gate** where every visitor enters and is pointed to a building | **API Gateway** |
| One **central notice board** for every building's rules | **Config Server** |
| A **tracking slip** that follows a visitor from building to building | **Distributed tracing** |

---

## Concept 1 — Monolith vs microservices

**Plain meaning:**
- **Monolith**: the whole application is **one** deployable program (Topics 1–7).
- **Microservices**: the application is **many small programs**. Each one does **one business job**, has **its own data**, can be **deployed alone**, and talks to the others over the network.

| | Monolith | Microservices |
|---|---|---|
| Deploy | One big jar | Many small jars |
| One crash | Everything is down | Only that service is down |
| Scaling | Copy everything | Copy only the busy service |
| Teams | Everyone in one codebase | One team per service |
| Complexity | **Simple** ✅ | **Network, discovery, failures...** ⚠️ |

**Real-life illustration:** one **giant school building** vs a **campus of separate buildings**. If the canteen floods, the exam centre keeps running. On exam day you can add a second exam hall without building a second canteen.

**Hold on to:** **microservices are not automatically better.** They trade simplicity for independence. Start with a well-structured monolith. Split when the size of the team or system really needs it.

---

## Concept 2 — Service-to-service calls over HTTP

**Plain meaning:** when the Enrollment service needs course information, it sends an **HTTP request** to the Catalog service, exactly like Postman or a browser would. The calling service is the **client**, and the other is the **server**.

```java
RestClient client = RestClient.create();
CatalogCourse course = client.get()
        .uri("http://localhost:8081/catalog/courses/{code}", "CS101")   // 👈 hard-coded address
        .retrieve()
        .body(CatalogCourse.class);
```

**The problem:** the address `localhost:8081` is **hard-coded**. In the cloud, services **move** (new machines, new ports) and run **many copies**. A hard-coded address breaks.

**Real-life illustration:** calling the canteen on a phone number **you memorised last year**. The canteen moved, and now there are two branches. Your memorised number is useless.

👉 Run `example/Example1_DirectHttpCall.java` (start the catalog service first).

**Hold on to:** services talk with **HTTP + JSON** (Topic 4 skills). But **fixed addresses don't survive the cloud**.

---

## Concept 3 — Service discovery (Eureka)

**Plain meaning:**
1. A **service registry** (we use **Netflix Eureka**) is a small server that keeps a live list: *service name → addresses of its running copies*.
2. Every service **registers itself** at start-up ("I'm `catalog-service`, at localhost:8081") and sends a **heartbeat** every few seconds ("still alive!").
3. A service that wants to call another **asks the registry by name**. If heartbeats stop, the registry removes that copy.

```java
@SpringBootApplication
@EnableEurekaServer              // this app IS the phone directory
public class ServiceRegistryApp { ... }
```
```properties
# in each service:
spring.application.name=catalog-service                       # the NAME others will use
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

**Real-life illustration:** the **campus phone directory**. Each building writes its current extension in the directory and confirms every morning that it's still there. You just look up **"Canteen"**.

**Hold on to:** **call services by NAME (`catalog-service`), never by address.** The registry knows the address.

---

## Concept 4 — Client-side load balancing

**Plain meaning:** if the registry lists **two** copies of `catalog-service` (ports 8081 and 8091), the caller **spreads its requests** across them (round-robin by default). In Spring Cloud this is done by **Spring Cloud LoadBalancer**. It happens automatically when you call by service name.

**Real-life illustration:** the canteen has **two branches**. The switchboard sends the first call to branch A, the next to branch B, and so on, so no branch is overwhelmed.

**Hold on to:** **more traffic? Start another copy.** It registers itself and immediately starts receiving its share. In the project, `servedByCatalogPort` shows you which copy answered.

---

## Concept 5 — Declarative HTTP clients (OpenFeign)

**Plain meaning:** instead of writing `RestClient` code for every call, you write an **interface** that describes the other service's endpoints. **Spring Cloud OpenFeign** generates the HTTP code at start-up, *and* it uses discovery and load balancing for you.

```java
@FeignClient(name = "catalog-service")                 // the NAME in Eureka, not an address!
public interface CatalogClient {

    @GetMapping("/catalog/courses/{code}")              // same annotations as a controller (Topic 4)
    CatalogCourse findCourse(@PathVariable("code") String code);
}

// Using it feels like calling a normal Java method:
CatalogCourse course = catalogClient.findCourse("CS101");
```

Switch it on with `@EnableFeignClients` on the main class.

**Real-life illustration:** a **speed-dial button** labelled "Canteen". You press it. It looks up the directory, picks a branch and dials. You never see the number.

**Hold on to:** **Feign = an interface that *is* an HTTP client.** It's like Spring Data JPA (Topic 7) for HTTP calls: you declare, Spring implements.

---

## Concept 6 — Resilience: circuit breaker + fallback

**Plain meaning:** in a network, **things fail**: a service crashes, is slow, or the network drops. Without protection, callers **wait and pile up** until *they* crash too. That's a **cascading failure**.

A **circuit breaker** wraps calls to another service and watches for failures:

| State | Meaning | Like a house breaker... |
|---|---|---|
| **CLOSED** | Everything is normal. Calls go through. | Power is on |
| **OPEN** | Too many recent failures → **stop calling** and **fail immediately** with the fallback | It has **tripped**. No power flows, so the fire can't spread |
| **HALF-OPEN** | After a wait, let a **few test calls** through. If they succeed → CLOSED, if they fail → OPEN again | You flip the switch back carefully to test |

A **fallback** is the **"Plan B" answer** returned when the call fails or the breaker is open:

```java
@FeignClient(name = "catalog-service", fallback = CatalogClientFallback.class)
public interface CatalogClient { ... }

@Component
public class CatalogClientFallback implements CatalogClient {
    public CatalogCourse findCourse(String code) {
        // Plan B: a special "catalog unavailable" answer. The enrollment is saved as PENDING instead of crashing.
        return new CatalogCourse(code, "CATALOG-UNAVAILABLE", null, 0);
    }
}
```
```properties
spring.cloud.openfeign.circuitbreaker.enabled=true   # wrap every Feign call in a circuit breaker
```

We use **Resilience4j** as the circuit breaker library (rules in `ResilienceConfig.java`).

**Real-life illustration:** the **house breaker** trips during a fault so the whole house doesn't burn. And when the canteen is closed, the **vending machine** (fallback) still gives you *something*.

👉 Run `example/Example2_CircuitBreakerIdea.java` to watch CLOSED → OPEN → HALF-OPEN → CLOSED happen.

**Hold on to:** **in microservices, plan for failure.** Fail fast and degrade gracefully. Never hang forever.

---

## Concept 7 — API Gateway (concept)

**Plain meaning:** outside clients (a mobile app, a website) shouldn't need to know 20 service addresses. An **API Gateway** is **one front door** (e.g. `api.school.com`). It **routes** each request to the right service by path:

```
Mobile app ──▶  Gateway  ──/catalog/**────▶ catalog-service
                         ──/enrollments/**─▶ enrollment-service
                         ──/results/**─────▶ results-service
```

The gateway is also the natural place for **security checks** (Topic 5: validate the login token once at the door), **rate limiting** and **CORS**. Spring's tool is **Spring Cloud Gateway**:

```properties
spring.cloud.gateway.server.webmvc.routes[0].id=catalog
spring.cloud.gateway.server.webmvc.routes[0].uri=lb://catalog-service      # lb:// = use discovery + load balancing
spring.cloud.gateway.server.webmvc.routes[0].predicates[0]=Path=/catalog/**
```

**Real-life illustration:** the **main campus gate**. Every visitor enters through one gate, shows ID once, and is directed to the right building.

**Hold on to:** **one public entrance, many private services behind it.** (We describe it here rather than run it, to keep the project small.)

---

## Concept 8 — Config Server, tracing and the rest of the toolbox (concept)

| Tool | Problem it solves | Campus story |
|---|---|---|
| **Spring Cloud Config Server** | Settings for many services × environments, kept in one place (often a Git repo) | One **central notice board** that every building reads |
| **Distributed tracing** (Micrometer Tracing + Zipkin) | "This request was slow. Which of the 6 services caused it?" | A **tracking slip** stamped by every building the visitor passes through |
| **Centralised logs** | Logs scattered over 20 machines | One **logbook room** for the whole campus |
| **Containers** (Docker) + **orchestration** (Kubernetes) | Running, restarting and scaling many services | **Portable cabins** (Topic 7's fat jar inside a container) and a **site manager** who keeps the right number of them running |
| **Messaging** (Kafka, RabbitMQ) | Services that shouldn't wait for each other | Leaving a **note in a pigeonhole** instead of phoning |

**Hold on to:** every one of these exists because **many small services** create problems that **one app** never had.

---

## Putting it all together (tell it as one story)

> "A **monolith** is one big app. **Microservices** split it into small, independent Spring Boot apps, each with one job and its own data. They talk over **HTTP**, but hard-coded addresses break, so every service **registers by name** in a **service registry** (Eureka) and finds the others there. If a service runs several copies, calls are **load-balanced** across them. With **OpenFeign** I call another service through a plain Java **interface**. Because networks fail, every call is wrapped in a **circuit breaker** with a **fallback**, so one failure can't bring everything down. Outside users come in through one **API Gateway**, settings live on a **Config Server**, and **tracing** follows a request across services."

---

## The project: a 3-app school system

```
                 ┌───────────────────────────┐
                 │  ServiceRegistryApp :8761 │  ◀── everyone registers here (Eureka)
                 └─────────▲───────▲─────────┘
                 register  │       │  register + look up "catalog-service"
       ┌───────────────────┘       └────────────────────┐
┌──────┴─────────────┐   Feign: GET /catalog/courses/X  ┌┴───────────────────────┐
│ CatalogServiceApp  │ ◀─────────────────────────────── │ EnrollmentServiceApp   │ ◀── POST /enrollments
│ :8081 (and :8091)  │   (load-balanced, with a         │ :8082                  │
└────────────────────┘    circuit breaker + fallback)   └────────────────────────┘
```

| App | Endpoint | Purpose |
|---|---|---|
| catalog | `GET /catalog/courses` | all courses |
| catalog | `GET /catalog/courses/{code}` | one course (404 if unknown) |
| enrollment | `POST /enrollments` `{"student":"ada","courseCode":"CS101"}` | enroll (asks the catalog first) |
| enrollment | `GET /enrollments` | list enrollments |

| Situation | Result |
|---|---|
| Catalog is up, course exists | `CONFIRMED` (shows which catalog port answered) |
| Catalog is up, course unknown | 400 "No such course" |
| Catalog is **down** | `PENDING` from the **fallback**: the system stays up |

---

# ✅ Test Yourself

## Part A — Quick check

1. What is a monolith? What is a microservice?
2. Give two advantages and two disadvantages of microservices.
3. Why is calling `http://localhost:8081` from another service a problem in the cloud?
4. What does a service registry store? What is a heartbeat?
5. In Eureka, what name does a service register under (which property)?
6. What does load balancing do when two copies of a service are registered?
7. What is OpenFeign? Which annotation switches it on?
8. What is a cascading failure?
9. Name the three states of a circuit breaker and what each means.
10. What is a fallback? Give a real-world example.
11. What does an API Gateway do? Name two things often done at the gateway.
12. What problem does a Config Server solve?

## Part B — Explain it like a teacher

13. Explain service discovery using a **taxi app** (or any example other than a school).
14. Explain a circuit breaker to a friend using a **football match** or a **kitchen**.
15. A small startup with 3 developers wants to begin with 15 microservices. What would you advise, and why?

## Part C — Hands-on tasks

16. Start the registry, the catalog and enrollment. Find both services on the Eureka dashboard. How long did it take for them to appear?
17. Enroll `ada` in `CS101`. Which port served the catalog call?
18. Start a second catalog on port 8091. Enroll 4 times. What do you notice about `servedByCatalogPort`?
19. Stop **all** catalogs. Enroll again. What status does the enrollment get, and which class produced it?
20. Restart a catalog and wait ~30 s. Enroll again. Explain what happened, in terms of breaker states.
21. Add a new course to `CatalogController` and enroll in it, without restarting the enrollment service. Why does that work?

---

<details>
<summary><b>👩‍🏫 Answer key (click to open)</b></summary>

1. A monolith is a whole application deployed as one program. A microservice is a small, independently deployable service that does one business job and owns its data.
2. Advantages: independent deployment and scaling, failure isolation, team independence, freedom of technology. Disadvantages: network complexity, harder debugging, data consistency across services, more infrastructure to run.
3. Services move and run several copies. A fixed address breaks when the service moves, and it can't spread load across copies.
4. Service names mapped to the network addresses of their running instances. A heartbeat is a regular "I'm still alive" message. If heartbeats stop, the instance is removed.
5. `spring.application.name`.
6. It spreads calls across the instances (round-robin by default), so each one gets part of the traffic.
7. A declarative HTTP client: you write an annotated interface and Spring generates the HTTP code. Switch it on with `@EnableFeignClients`.
8. One failing or slow service makes its callers wait and fail, which makes *their* callers fail, and so on, until the whole system is down.
9. **CLOSED** (calls pass normally), **OPEN** (calls are blocked and fail fast with the fallback), **HALF-OPEN** (a few trial calls test whether the service has recovered).
10. A backup answer used when the real call fails. E.g. Netflix showing a generic "popular movies" list when the personalised recommendation service is down.
11. It's a single entry point that routes requests to services. Common gateway jobs: authentication, rate limiting, CORS, logging.
12. Managing settings for many services and environments in one central place, with changes visible to all services.
13. Taxi app: you don't phone a specific driver's number. You ask the app (the registry) for "a driver near me". Drivers register their live location (heartbeat), and the app gives you one that's currently available.
14. Kitchen: if the oven keeps burning food, the head chef says "stop sending orders to the oven for 10 minutes" (OPEN) and serves salads instead (fallback). Then they try one dish (HALF-OPEN), and if it comes out fine, they go back to normal (CLOSED).
15. Start with a **well-structured monolith** (clear packages/layers). Microservices add a lot of operational work that a tiny team can't afford. Split later, when there's a real need.
16. Usually 5–30 seconds, depending on the registration and heartbeat intervals.
17. 8081 (the only catalog running).
18. It alternates between 8081 and 8091: client-side load balancing.
19. `PENDING`, produced by `CatalogClientFallback` (the fallback) through the circuit breaker.
20. While the catalog was down, calls failed and the breaker opened. After the wait time it went HALF-OPEN, the test call succeeded (the catalog is back and registered), and it CLOSED again, so you get `CONFIRMED`.
21. The enrollment service asks the catalog live on every call. It doesn't keep its own copy of the course list, because each service owns its own data.
</details>
