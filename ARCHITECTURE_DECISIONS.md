# Movie Ticket Booking System — Architecture Decisions

This document captures every architecture and technology decision made during design, along with the reasoning. Intended to be folded into the final README.md.

## Tech stack

| Concern | Choice |
|---|---|
| Language / framework | Java + Spring Boot |
| Build tool | Maven |
| Database | Oracle SQL |
| Cache / distributed lock | Redis |
| Event bus | Apache Kafka |
| Auth | JWT (access + refresh token pair) |
| API docs | springdoc-openapi (Swagger UI) |

## Seat booking concurrency

Two-layer locking to serialize concurrent booking attempts on the same seat:

1. **Redis distributed lock** — acquired the instant a user selects a seat (key scoped per show + seat), held for the duration of the hold window. This is what makes seat selection fast and correctly serialized across multiple app instances without hitting the DB on every selection attempt.
2. **DB pessimistic lock** (`SELECT ... FOR UPDATE`) — applied at final payment confirmation, inside a DB transaction, as the final source of truth. Even if something raced past the Redis layer, this is the last line of defense before a booking is committed.

## Seat hold expiry

Three independent layers, increasing in reliability, decreasing in speed:

1. **Primary — Redis delayed queue**: manually implemented using a Redis sorted set (ZSET), scored by hold-expiry timestamp, with a custom polling worker (`ZRANGEBYSCORE`) — no third-party library (Redisson considered, rejected in favor of full control and no extra dependency). Chosen over simple TTL + keyspace notifications because Redis pub/sub is fire-and-forget — an event can be silently lost if a subscriber is briefly offline. The ZSET approach is durable: nothing is lost, a worker just picks it up on its next poll.
2. **Event broadcast — Kafka**: once the Redis worker (or the cron fallback) detects an expired hold, it publishes a `SeatHoldExpired` event to Kafka. Kafka is *not* used to implement the delay itself (it has no native delay/TTL mechanism) — it's used purely as a reliable event bus so every interested downstream consumer (seat-release handler, audit log, analytics) gets the event with retry/replay semantics.
3. **Safety net — cron fallback**: a scheduled job runs every 3 minutes, scanning the DB directly for holds created more than 10 minutes ago. Catches anything the above two layers might miss.

## Payment

Mocked, since real gateways are out of scope. Behavior:

- Booking-confirm call returns immediately (e.g. `PENDING_PAYMENT` status) — the user isn't blocked waiting on payment processing.
- An async worker simulates gateway processing, then calls back (in-process, since there's no real external gateway) to flip the booking to `CONFIRMED` / `FAILED`.
- **Idempotency key** required on payment/booking-confirm endpoints, so retries (network blips, client double-submits) can't double-charge or double-book.

## Notifications

- Delivered via the same Kafka pipeline used for hold-expiry events — confirmations and reminders are published as events, consumed asynchronously, never blocking the booking flow.
- Channels: both email and SMS stubs implemented, channel configurable per notification type.

## Pricing & discounts

- Multiple pricing tiers: regular, premium, weekend.
- Discount codes: rule engine implemented as plain Java (Strategy/Specification pattern), not a third-party rules library. Rejected Easy Rules — the four condition types (date range, min seats, tier-specific, usage limits) are fixed and known at compile time, not admin-authored at runtime, so a full rules engine would be unjustified abstraction for this scope.
- Composable: rules stack (e.g. a code can require both a date range AND a minimum seat count).

## Refunds

- Time-based tiered structure (e.g. >24h before show = 100%, 12–24h = 50%, <12h = 0%) — the rule *structure* is fixed (time-based only, not a composable engine like discounts), but the tier values themselves are admin-CRUD-able via the API, per the original requirement that admins manage refund policies.

## Seat layouts

- Flexible JSON-based seat map per theater — supports irregular row lengths, aisles, and curved-screen layouts, rather than a forced uniform rows × columns grid.

## Auth / RBAC

- JWT-based. Access + refresh token pair, refresh token rotation on use, and a revocation list (e.g. for logout / compromised token invalidation).
- Roles: `ADMIN`, `CUSTOMER`.

## Testing strategy

No Docker dependency — everything runs via mocks/embedded fakes:

| Dependency | Test substitute | Caveat |
|---|---|---|
| Oracle | H2 in Oracle-compatibility mode | Does not perfectly replicate Oracle's `SELECT FOR UPDATE` locking semantics. Pessimistic-lock tests verify the code path is correct, not byte-for-byte real-Oracle locking behavior. Documented here as a known, accepted gap. |
| Redis | Embedded-Redis library | Real in-process Redis server — closer to production behavior than mocking the client. |
| Kafka | Spring `@EmbeddedKafka` | True in-process broker, no caveats. |

## API documentation

- `springdoc-openapi` — auto-generated Swagger UI from controller annotations.

## Not yet decided

- Entity / ERD design
- API endpoint contracts
- Package structure
- Reminder notification timing (how long before showtime)
