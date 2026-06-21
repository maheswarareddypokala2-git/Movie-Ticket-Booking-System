# AGENTS.md

Guidance for any AI coding agent (or human) picking up this repository. Read this before writing code. Full design rationale lives in `README.md` and `docs/`.

## Project

A movie ticket booking system (Spring Boot, SDE-2 take-home assignment). Multiple cities, theaters, screens, shows; seat-level booking with time-bound holds; tiered pricing and discount codes; mocked async payment; configurable refund policies; concurrency-safe seat locking. See `README.md` for the complete brief, architecture, data model, and API contracts.

## Setup & commands

```bash
mvn clean install        # build
mvn spring-boot:run      # run (needs Oracle, Redis, Kafka reachable — see below)
mvn test                 # run tests (no external services required)
```

Swagger UI once running: `http://localhost:8080/swagger-ui.html`

**To run the app** (not needed for tests): a reachable Oracle instance, Redis on `localhost:6379`, Kafka on `localhost:9092` — or override `spring.datasource.*`, `spring.data.redis.*`, `spring.kafka.bootstrap-servers` in `application.yml`. Replace the `CHANGE_ME` placeholders before connecting to real infrastructure; never commit real credentials.

## Code style & conventions

- Package root: `com.takehome.moviebooking`
- Strict layering: `controller` → `service` → `repository` → `domain`. Controllers do not contain business logic; services do not contain HTTP concerns.
- Constructor injection only — no field injection (`@Autowired` on fields).
- Never expose JPA entities directly from controllers — always map to/from `dto/request` and `dto/response` classes.
- Lombok for boilerplate (`@Getter`/`@Setter`/`@Builder` etc.) is fine; don't hand-write getters/setters.
- Input validation via `jakarta.validation` annotations on request DTOs; errors handled by a single global `@ControllerAdvice` exception handler — no scattered try/catch in controllers.
- UUID primary keys throughout, generated consistently (pick one `@GeneratedValue` strategy and use it everywhere — don't mix).

## Non-negotiable architecture constraints

These were reached through an explicit, decision-by-decision conversation with the project owner — not defaults, not guesses. **Do not silently replace or "improve" them.** If one seems wrong once you're implementing it, raise it as a question rather than changing it.

- **Database is Oracle SQL.** Tests substitute H2 in Oracle-compatibility mode (no Docker available) — this is an accepted, documented gap, not something to "fix" by switching to Testcontainers.
- **Seat concurrency is two-layer**: Redis distributed lock (`SET ... NX EX`) at seat selection, DB pessimistic lock (`SELECT ... FOR UPDATE`) at final booking confirmation. Both layers are required — do not collapse to just one.
- **Seat hold expiry is three-layer**: a manually-implemented Redis ZSET delay queue (no Redisson or other delay-queue library) is the primary mechanism; Kafka is an event *bus* only (it has no native delay support — do not attempt to implement the delay inside Kafka); a 3-minute DB cron scan is the safety net. All three stay.
- **Discount rule engine is plain Java** (Strategy/Specification pattern). Do not introduce a rules-engine library (Easy Rules, Drools, etc.) — this was explicitly evaluated and rejected because the four rule types are fixed and known at compile time.
- **Refund policy structure is fixed** (time-based tiers only), but tier *values* are admin-CRUD-able via the API — don't make the structure itself configurable/composable.
- **Payment is async**: booking confirmation returns immediately in `PENDING_PAYMENT`; a worker resolves it later. Don't make this synchronous even for simplicity.
- **Idempotency key is required** on the booking-confirm endpoint.
- **Auth is JWT with access + refresh token pair, refresh rotation, and a revocation list.** Not OAuth/SSO/MFA (explicitly out of scope per the assignment).
- **No Docker, containerization, or CI/CD** — explicitly out of scope per the assignment. Tests must run with `mvn test` alone.

## Where to find things

- Full architecture, data model, API contracts, detailed flow walkthroughs: `README.md`
- Original decision-by-decision rationale: `docs/ARCHITECTURE_DECISIONS.md`
- API contract reference: `docs/API_CONTRACTS.md`
- Ordered implementation roadmap: `README.md` Section 10 — work through it top to bottom; later steps depend on earlier ones

## Testing

- `mvn test` must pass with zero external services running (no Docker, no real Oracle/Redis/Kafka) — see `src/test/resources/application-test.yml`.
- Any service method touching money (pricing, discounts, refunds) or concurrency (seat hold, booking confirm) needs a dedicated test.
- Concurrency-critical paths need a test that actually simulates concurrent requests (e.g. multiple threads racing for the same seat) — a single-threaded happy-path test is not sufficient coverage for those.

## Commits

Multiple commits are an explicit submission requirement — commit per logical unit of work (e.g. "Add SeatHold entity and repository"), not one giant commit at the end.
