# Movie Ticket Booking System — API Contracts

All endpoints under `/api`. Auth via `Authorization: Bearer <access_token>` unless marked public. Roles: `ADMIN`, `CUSTOMER`.

## Auth (public)

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Create a customer account |
| POST | `/api/auth/login` | Returns access + refresh token pair |
| POST | `/api/auth/refresh` | Rotates refresh token, returns new access + refresh pair |
| POST | `/api/auth/logout` | Revokes the current refresh token (adds to revocation list) |

## Admin — catalog management (`ROLE_ADMIN`)

| Method | Path | Description |
|---|---|---|
| POST | `/api/admin/cities` | Create city |
| GET | `/api/admin/cities` | List cities |
| PUT | `/api/admin/cities/{id}` | Update city |
| DELETE | `/api/admin/cities/{id}` | Delete city |
| POST | `/api/admin/theaters` | Create theater (`cityId` in body) |
| GET | `/api/admin/theaters?cityId=` | List theaters, optionally by city |
| PUT | `/api/admin/theaters/{id}` | Update theater |
| DELETE | `/api/admin/theaters/{id}` | Delete theater |
| POST | `/api/admin/theaters/{theaterId}/screens` | Create screen with JSON seat-layout map; generates `Seat` rows from the layout |
| GET | `/api/admin/screens/{id}` | Get screen + layout |
| PUT | `/api/admin/screens/{id}/layout` | Replace seat layout (regenerates seats; blocked if active bookings exist) |
| POST | `/api/admin/movies` | Create movie |
| GET | `/api/admin/movies` | List movies |
| PUT | `/api/admin/movies/{id}` | Update movie |
| POST | `/api/admin/shows` | Create show (`screenId`, `movieId`, `startTime`) |
| GET | `/api/admin/shows` | List shows (admin view, all statuses) |
| PUT | `/api/admin/shows/{id}` | Update show |
| DELETE | `/api/admin/shows/{id}` | Cancel/delete show |

## Admin — pricing & policy management (`ROLE_ADMIN`)

| Method | Path | Description |
|---|---|---|
| POST | `/api/admin/pricing-policies` | Create tier pricing (tier, weekday price, weekend price) |
| GET | `/api/admin/pricing-policies` | List pricing policies |
| PUT | `/api/admin/pricing-policies/{id}` | Update pricing policy |
| POST | `/api/admin/discount-codes` | Create discount code with stacked rule conditions |
| GET | `/api/admin/discount-codes` | List discount codes |
| PUT | `/api/admin/discount-codes/{id}` | Update discount code |
| DELETE | `/api/admin/discount-codes/{id}` | Deactivate discount code |
| POST | `/api/admin/refund-policies` | Create a refund tier (hours-before-show range → refund %) |
| GET | `/api/admin/refund-policies` | List refund tiers |
| PUT | `/api/admin/refund-policies/{id}` | Update a refund tier's values |
| DELETE | `/api/admin/refund-policies/{id}` | Remove a refund tier |

## Customer — browse (public, no auth required)

| Method | Path | Description |
|---|---|---|
| GET | `/api/cities` | List cities |
| GET | `/api/theaters?cityId=` | List theaters in a city |
| GET | `/api/shows?cityId=&movieId=&date=` | Search shows |
| GET | `/api/shows/{id}` | Show details (movie, screen, timing, pricing) |
| GET | `/api/shows/{id}/seats` | Live seat map: `AVAILABLE` / `HELD` / `BOOKED` per seat |

## Customer — booking flow (`ROLE_CUSTOMER`)

| Method | Path | Description |
|---|---|---|
| POST | `/api/shows/{showId}/seats/{seatId}/hold` | Acquire Redis lock + create `SeatHold`; returns `holdId`, `expiresAt` |
| DELETE | `/api/holds/{holdId}` | Release a hold early (user deselects a seat) |
| POST | `/api/bookings` | Confirm booking from a set of active `holdIds` + optional `discountCode`. **Requires `Idempotency-Key` header.** Returns booking in `PENDING_PAYMENT`; payment resolves asynchronously |
| GET | `/api/bookings/{id}` | Booking status and details |
| GET | `/api/bookings` | Current user's booking history |
| POST | `/api/bookings/{id}/cancel` | Cancel a confirmed booking; computes refund via the applicable `RefundPolicy` tier |

## Internal (not exposed externally)

| Component | Description |
|---|---|
| Mock payment worker | Triggered after `POST /api/bookings`; simulates gateway processing, then transitions booking to `CONFIRMED` or `FAILED` |
| Redis ZSET delay-queue worker | Polls for expired holds, publishes `SeatHoldExpired` to Kafka |
| Cron fallback job | Runs every 3 minutes; DB-level safety net for holds older than 10 minutes |
| Kafka consumers | Seat-release handler, notification dispatcher (email/SMS stubs) |

## Not yet decided

- Exact request/response DTO field names and validation rules
- Pagination/sorting conventions for list endpoints
- Error response schema
