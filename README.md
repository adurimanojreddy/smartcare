# SmartCare — Intelligent Hospital Appointment & Queue Management System

A full-stack hospital appointment platform built as a portfolio/academic
project: patients get a rule-based department suggestion, book a slot,
pay (mock gateway), check in with a QR code, and track their live queue
position — while doctors run their day from a queue console and admins
watch hospital-wide analytics.

> **Not a real medical system.** Department/slot suggestions are
> rule-based appointment guidance, not a diagnosis. Payments use a mock
> gateway with no real transactions or stored card data. There is no
> ABHA/ABDM integration. See [About](#important-constraints) below.

---

## Table of contents

- [Project overview](#project-overview)
- [Features](#features)
- [Technology stack](#technology-stack)
- [System architecture](#system-architecture)
- [Database schema](#database-schema)
- [API documentation](#api-documentation)
- [Installation](#installation)
- [Configuration](#configuration)
- [Test credentials](#test-credentials)
- [Running tests](#running-tests)
- [Screenshots](#screenshots)
- [Important constraints](#important-constraints)
- [Future enhancements](#future-enhancements)

---

## Project overview

SmartCare has three roles — **Patient**, **Doctor**, **Admin** — each with
their own dashboard, backed by a single Spring Boot REST API and a
Bootstrap-free (hand-rolled) HTML/CSS/JS frontend served from the same
application.

The centerpiece features are:

- **Smart department recommendation** — a configurable, database-driven
  symptom→department weighting table (not a hardcoded if/else chain, and
  not a diagnostic model).
- **Smart slot recommendation** — matches a patient's preferred time
  window against a doctor's real availability.
- **QR check-in + live queue** — booking generates a QR code; scanning it
  assigns a per-doctor/per-day token and the patient watches their queue
  position update in near-real-time.
- **Cancellation slot recovery** — cancelling or rescheduling an
  appointment automatically notifies other patients who asked to be told
  about earlier openings with that doctor.
- **Family accounts** — one patient login can manage appointments and
  medical history for dependents.

## Features

See the original project brief for the exhaustive feature list per role;
in short:

| Role | Highlights |
|---|---|
| Patient | Search doctors, symptom-based department suggestion, book/reschedule/cancel, QR check-in, live queue position, family accounts, medical timeline, prescriptions, mock payments, notifications |
| Doctor | Manage weekly schedule, generate bookable slots, run the live queue (call next / start / complete / skip / no-show), write digital prescriptions, request lab tests |
| Admin | Manage doctors/departments, browse any doctor's appointments, hospital-wide dashboard and analytics (cancellation/no-show rates, doctor utilization, most-booked departments, revenue) |

## Technology stack

**Backend:** Java 17, Spring Boot 3.3, Spring MVC, Spring Data JPA /
Hibernate, Spring Security + JWT (jjwt), Bean Validation, MySQL, Lombok,
MapStruct (declared, manual mappers used for reliability), ZXing (QR
codes), Maven.

**Frontend:** HTML5, CSS3 (hand-written design system — no framework),
vanilla JavaScript (ES6+), Chart.js (admin analytics only). No React, per
the project brief.

**Testing:** JUnit 5, Mockito, AssertJ, Spring Boot Test (`@DataJpaTest`,
`@SpringBootTest` + MockMvc), H2 (test-only in-memory database).

## System architecture

Clean layered architecture, one direction of dependency:

```
Controller → Service → Repository → Entity
                 │
                 ├── DTO (API never exposes entities directly)
                 ├── Mapper (entity ⇄ DTO, plain static methods)
                 ├── Exception (custom exceptions + @RestControllerAdvice)
                 └── Security (JWT filter, UserPrincipal, role guards)
```

- **Stateless JWT auth**: every request carries `Authorization: Bearer
  <token>`; `JwtAuthenticationFilter` populates the `SecurityContext`.
  `@PreAuthorize("hasRole('...')")` on each controller method enforces
  role separation (patients can't hit doctor/admin endpoints, etc.).
- **Booking concurrency**: slot booking takes a pessimistic DB lock
  (`SELECT ... FOR UPDATE` via `@Lock(PESSIMISTIC_WRITE)`) plus the
  `AppointmentSlot` entity's own optimistic `@Version` column, plus a
  unique `(doctor, date, start_time)` DB constraint, plus an explicit
  double-booking existence check — four independent guards against two
  patients winning the same slot.
- **Ownership checks** happen in the service layer against the
  authenticated principal (`SecurityUtils.getCurrentUserId()`), not
  trusted from the URL — a patient can't view another patient's
  appointment just by guessing an ID.

## Database schema

17 entities (see `entity/` and `entity/enums/`):

```
User ─┬─ Patient ─┬─ FamilyMember
      └─ Doctor ──┴─ DoctorSchedule ── AppointmentSlot
Department ── DoctorSchedule / Appointment / DepartmentRecommendation
Appointment ─┬─ Queue
             ├─ Payment
             ├─ Prescription ── PrescriptionMedicine
             ├─ MedicalRecord
             └─ LabTest
Symptom ── DepartmentRecommendation ── Department
Notification ── User
```

Key constraints:
- `appointment_slots`: unique `(doctor_id, slot_date, start_time)`, `@Version` optimistic lock.
- `appointments`: unique `appointment_code`.
- `department_recommendations`: unique `(symptom_id, department_id)`.
- `users`: unique `email`.

`ddl-auto: update` creates/updates the schema on startup for this
project's purposes; switch to `validate` + a migration tool (Flyway/
Liquibase) before treating this as production-grade.

## API documentation

Full endpoint list lives in the controllers (`controller/` package);
summary:

```
POST   /api/auth/register              Patient self-registration
POST   /api/auth/login                 Login (any role)

GET    /api/departments                Public browse
POST   /api/departments                Admin create
GET    /api/doctors                    Public browse
GET    /api/doctors/search             ?name=&specialization=&departmentId=
POST   /api/doctors                    Admin creates a doctor account

POST   /api/doctors/{id}/schedules     Doctor/admin sets weekly availability
POST   /api/doctors/{id}/slots/generate?from=&to=   Generate bookable slots
GET    /api/doctors/{id}/slots?date=   Available slots for a date

POST   /api/recommendations/department Symptom → department (Module 6)
GET    /api/recommendations/slots      Preferred time → best slot (Module 7)

POST   /api/appointments               Book
GET    /api/appointments/{id}
POST   /api/appointments/{id}/cancel
POST   /api/appointments/{id}/reschedule
POST   /api/appointments/qr-check-in/{appointmentCode}   QR scan check-in

GET    /api/queue/{doctorId}                        Doctor's live queue
POST   /api/queue/{doctorId}/call-next
POST   /api/queue/{appointmentId}/start
POST   /api/queue/{appointmentId}/complete
GET    /api/queue/appointment/{id}/status            Patient's live position

POST   /api/payments                   Create (mock gateway)
POST   /api/payments/{id}/process

POST   /api/prescriptions              Doctor writes one
GET    /api/prescriptions/patient/{patientId}

GET    /api/family-members
POST   /api/family-members

GET    /api/admin/dashboard
GET    /api/admin/analytics?from=&to=
```

A ready-to-import **Postman collection** with a full example flow
(register → login → create department/doctor → generate slots → book →
attempt a double-booking (expects `409`) → check in → view queue → admin
dashboard) is at `docs/SmartCare.postman_collection.json`.

### Sample request/response

```http
POST /api/appointments
Authorization: Bearer <patient JWT>
Content-Type: application/json

{
  "doctorId": 3,
  "slotId": 128,
  "consultationType": "IN_PERSON",
  "notifyOnEarlierSlot": true
}
```

```json
{
  "id": 42,
  "appointmentCode": "HSP-2026-000123",
  "status": "CONFIRMED",
  "paymentStatus": "NOT_APPLICABLE",
  "doctorName": "Kumar",
  "departmentName": "Dermatology",
  "appointmentDate": "2026-09-20",
  "appointmentTime": "10:30:00",
  "qrCodeImageBase64": "iVBORw0KGgoAAAANSUhEUgAA...",
  "notifyOnEarlierSlot": true
}
```

Error responses are always shaped like this (see `GlobalExceptionHandler`):

```json
{
  "timestamp": "2026-09-20T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "This slot is no longer available",
  "path": "/api/appointments"
}
```

## Installation

### Prerequisites
- JDK 17+
- Maven 3.9+ (or use your IDE's bundled Maven)
- MySQL 8+ running locally (or update `application.yml` to point elsewhere)

### Steps

```bash
# 1. Create the database (or let the app create it - see below)
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS smartcare_db;"

# 2. Configure credentials (see Configuration below), then build
cd smartcare
mvn clean install

# 3. Run
mvn spring-boot:run
```

The app serves both the API and the static frontend on the same port:
open **http://localhost:8080** for the frontend, and hit `/api/...` for
the REST API.

On first startup, `data.sql` seeds:
- An admin account (see [Test credentials](#test-credentials))
- 9 departments
- 14 symptoms with department-recommendation weightings

## Configuration

All configuration lives in `src/main/resources/application.yml`, with
environment-variable overrides for anything sensitive:

| Setting | Env var | Default |
|---|---|---|
| DB URL | — | `jdbc:mysql://localhost:3306/smartcare_db` |
| DB username | `DB_USERNAME` | `root` |
| DB password | `DB_PASSWORD` | `root` |
| JWT secret | `JWT_SECRET` | demo value — **change this** |
| JWT expiry | `JWT_EXPIRATION_MS` | 86400000 (24h) |
| CORS origins | `CORS_ALLOWED_ORIGINS` | localhost:8080/5500 |
| Cancellation cutoff | — | 2 hours (`smartcare.business.cancellation-cutoff-hours`) |
| No-show risk thresholds | — | configurable under `smartcare.noshow.*` |

## Test credentials

Seeded by `data.sql`:

| Role | Email | Password |
|---|---|---|
| Admin | `admin@smartcare.local` | `Admin@12345` |

Doctor and patient accounts don't come pre-seeded — create a patient via
**Register**, and create a doctor via the admin's **Manage Doctors**
page (or `POST /api/doctors`).

**Change the admin password / JWT secret before deploying this anywhere
real.**

## Running tests

```bash
mvn test
```

Covers (per the project brief's Testing module):
- **Authentication tests** — registration, duplicate email, login success/failure (`AuthServiceImplTest`, `AuthControllerIT`)
- **Appointment booking tests** — happy path, past-slot rejection, fee-based payment gating (`AppointmentServiceImplTest`)
- **Double-booking tests** — slot already booked, doctor already booked at that time (`AppointmentServiceImplTest`)
- **Queue tests** — token numbering, position/wait-time math, call-next, cross-doctor authorization (`QueueServiceImplTest`)
- **Service-layer tests** — no-show risk rules, recommendation engine scoring (`NoShowRiskServiceImplTest`, `RecommendationServiceImplTest`)
- **Repository tests** — unique constraint enforcement, finder queries against H2 (`AppointmentSlotRepositoryTest`)
- **Controller tests** — full Spring Security filter chain, MockMvc (`AuthControllerIT`)

## Screenshots

Not included in this text-based deliverable — run the app and visit
`http://localhost:8080` to see the homepage, booking flow, live queue
ticket, and admin analytics charts firsthand. (Suggested for your own
README: a homepage hero screenshot, the booking stepper, the queue token
ticket, and the admin analytics dashboard.)

## Important constraints

This is a **portfolio/academic project**, not a production medical
system:
- Department/slot suggestions are rule-based guidance only — never a
  medical diagnosis.
- Payments use a mock gateway (`PaymentServiceImpl`) — no real
  transactions, no card data stored or accepted anywhere.
- No real ABHA/ABDM (India's national health record system) integration.
- No-show risk scoring is explicit rule-based logic, not machine
  learning.

## Future enhancements

- WebSocket/STOMP push for live queue updates instead of polling.
- Real payment gateway integration behind the existing `PaymentService`
  abstraction (only `processPayment()` would need to change).
- Flyway/Liquibase migrations instead of `ddl-auto: update`.
- Doctor-patient "currently treating" checks for medical-history access,
  beyond the current role-level checks.
- Push/SMS notification delivery (currently in-app only).
- Real-time video for online consultations (currently a placeholder link).
