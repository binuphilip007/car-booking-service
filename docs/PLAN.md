# Velocity Motors — Car Rental Booking Plan

Reconstructed roadmap for the Car Rental Booking assignment, covering both
`car-booking-service` and the sibling `credit-card-validation-service`.

## Phases

| Phase | Scope | Status |
|-------|-------|--------|
| 1 | `car-booking-service`: booking creation with `DIGITAL_WALLET` / `CASH` payment modes, H2 in-memory DB, validation (vehicle exists, rental dates, no overlapping booking) | Done |
| 2 | `credit-card-validation-service`: standalone service, contract-first from OpenAPI spec, H2-seeded mock payment cards | Done |
| 3 | `car-booking-service`: `CREDIT_CARD` payment mode — synchronous call to `credit-card-validation-service`, guarded by Resilience4j retry + circuit breaker | Done |
| 4 | `car-booking-service`: `BANK_TRANSFER` payment mode — booking starts `PENDING_PAYMENT` | Done |
| 5 | `car-booking-service`: Kafka listener (`bank-transfer-payment-events`) confirms bank-transfer bookings on matching payment event | Done |
| 6 | `car-booking-service`: `BankTransferBookingCancellationScheduler` auto-cancels unpaid bank-transfer bookings within 48h of rental start | Done |

## Key decisions
- Start with H2 in-memory DB for both services; Postgres migration deferred.
- `credit-card-validation-service` built as a separate Spring Boot project (not a module) to keep it independently deployable, mirroring the real microservice boundary.
- Booking/package naming: root package `com.velocitymotors.carbooking`; layered folders `controller/`, `service/(+impl)`, `repository/(+impl)`, `model/{api,entity,event}`, `validator/` — no `vo`/`bo`/`so` naming.
- Booking IDs generated via a DB sequence (`BookingIdSequenceJpaRepository`), not an in-memory counter, to survive restarts and avoid collisions.

## Next steps / open items
- Evaluate Postgres migration when moving beyond local/demo use.
- Track any new payment modes or validation rules here before implementing, so both services stay in sync.
