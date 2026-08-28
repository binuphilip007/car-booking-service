# Car Booking Service

Phase 1 of the car rental assignment is implemented as a Spring Boot service with an H2 in-memory database.

## Requirements

- Java 17
- Maven

## Run

```text
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

The default profile is `local`, which uses the H2 in-memory database and enables the H2 console. To select a profile explicitly:

```text
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Tests use the `test` profile and a separate H2 database. The H2 console is disabled for tests.

## Confirm a booking

```http
POST http://localhost:8080/api/v1/bookings
Content-Type: application/json
```

```json
{
  "customerName": "Binu Philip",
  "vehicleId": "VH1001",
  "rentalStartDate": "2026-09-01T10:00:00",
  "rentalEndDate": "2026-09-05T10:00:00",
  "vehicleCategory": "SUV",
  "paymentMode": "DIGITAL_WALLET",
  "paymentReference": "WALLET123"
}
```

The response is `201 Created` with a `CONFIRMED` booking. Valid mock vehicle IDs are `VH1001`, `VH1002`, and `VH1003`.

Payment behavior:

- `CASH` and `DIGITAL_WALLET` bookings are confirmed immediately.
- `CREDIT_CARD` bookings are confirmed only when the validation service returns `APPROVED`.
- `BANK_TRANSFER` bookings start as `PENDING_PAYMENT` and are confirmed by a full-payment event.

Bank-transfer cancellation uses the simple date-based assumption that rental start occurs at midnight. A pending bank-transfer booking is cancelled when its rental start date is within two calendar days of the current system date. The service does not compare payment amounts because the booking API does not capture an expected amount.

## H2 console

The H2 console is enabled at `http://localhost:8080/h2-console`.

Use this JDBC URL:

```text
jdbc:h2:mem:bookingdb
```

Username: `sa`  
Password: empty

## Test

```text
mvn test
```