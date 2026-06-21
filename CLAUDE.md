# CLAUDE.md

This file provides guidance to Claude Code when working in this repository.

@AGENTS.md

## How this project was designed

Before any code was written, every architecture and technology decision — database, concurrency strategy, hold-expiry mechanism, notification transport, auth design, discount and refund rule design, testing strategy, API documentation, idempotency, seat-layout modeling — was made through an explicit, turn-by-turn consultative process with the project owner. The AI proposed options and trade-offs at each fork; the owner chose; nothing was decided unilaterally. Where a choice had real engineering trade-offs (e.g. Kafka has no native delay mechanism, so the seat-hold-expiry design needed clarifying), those trade-offs were explained before a decision was made, not after.

The full decision log is in `docs/ARCHITECTURE_DECISIONS.md` and restated in `README.md`. Treat those as settled requirements, not suggestions — see the "Non-negotiable architecture constraints" section in `AGENTS.md`.

## Working in this repo with Claude Code

- Start with `README.md` Section 10 (Implementation roadmap) and work through it in order — later steps assume earlier ones exist (entities before repositories, security before RBAC-protected controllers, Redis/Kafka config before the services that use them).
- Before implementing any given step, read `README.md` Section 6 (Detailed flow walkthroughs) for the exact expected behavior. These were written to remove ambiguity, not as a rough sketch — follow them precisely (e.g. the exact order of operations in booking confirmation, including where the pessimistic lock happens relative to discount calculation).
- If something in the roadmap is ambiguous or under-specified once you're actually writing code, say so and ask rather than guessing. This project was built on a "no silent decisions" principle from the very first message — that should continue through implementation, not stop once design ended.
- After any meaningful change, run `mvn test`. The suite is designed to need zero external services — if a new test requires Docker or a real Oracle/Redis/Kafka instance to pass, that's a signal something has drifted from the agreed design.
- Keep commits small and logical (see `AGENTS.md`) — this is an explicit submission requirement for the assignment this project was built for, not just good practice.
