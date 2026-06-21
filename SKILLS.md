# Skills Used During Development

## AI tool skills

This project's design phase was built through a structured conversation with Claude (Sonnet 4.6), using the following capabilities:

- **Consultative elicitation tool** — used throughout to surface every technology and architecture decision (database, concurrency strategy, hold-expiry mechanism, notification transport, auth design, discount/refund rule design, testing strategy, etc.) as an explicit choice with trade-offs presented, rather than letting the AI decide unilaterally. This is the mechanism behind the entire decision log in `docs/ARCHITECTURE_DECISIONS.md`.
- **Diagram/visualization tool** — used to render the layered architecture diagram (and its revisions as decisions like Redis/Kafka were locked in) and the full entity-relationship diagram, both inline during the conversation and as Mermaid source embedded in `README.md` so they render natively on GitHub.
- **File and code generation tools** — used to scaffold the Maven project (`pom.xml`, package structure, `application.yml`/`application-test.yml`, `.gitignore`) and to produce the documentation set (`README.md`, `docs/ARCHITECTURE_DECISIONS.md`, `docs/API_CONTRACTS.md`, `AGENTS.md`, `CLAUDE.md`).
- **Shell/bash execution** — used to create the directory tree and package the final deliverable for review.

## Technical skills exercised

- **System design & distributed systems**: two-layer concurrency control (Redis distributed locking + DB pessimistic locking), event-driven architecture for hold-expiry and notifications, idempotency design for a payment callback path, reasoning about failure modes across three independent layers (Redis ZSET, Kafka, cron fallback).
- **Database design**: entity-relationship modeling for a multi-tenant-style domain (cities → theaters → screens → shows → seats), schema design for Oracle SQL, JSON-column modeling for a flexible seat layout.
- **Spring Boot ecosystem**: Spring Data JPA, Spring Security (JWT-based RBAC), Spring Kafka, Spring Data Redis, springdoc-openapi.
- **API design**: REST resource modeling, role-based endpoint segregation (admin vs. customer vs. public), request/response contract definition ahead of implementation.
- **Authentication design**: JWT access/refresh token pairs, refresh-token rotation, and revocation-list design (without reaching for OAuth/SSO, which was explicitly out of scope).
- **Software design patterns**: Strategy/Specification pattern for a composable discount rule engine, evaluated against and consciously chosen over a third-party rules-engine library.
- **Testing strategy**: designing a fully Docker-free test setup (H2 in Oracle-compatibility mode, embedded Redis, Spring's `@EmbeddedKafka`), including explicitly documenting the fidelity gap between H2 and real Oracle locking semantics rather than hiding it.
- **Technical documentation**: producing a self-contained README intended to let any engineer (or AI agent) resume the project with no additional context — including a full data dictionary, detailed flow walkthroughs, and an ordered implementation roadmap.
