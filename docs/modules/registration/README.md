# Registration Module

## Purpose

The Registration Module manages **workshop registrations and attendance tracking**. It handles the full lifecycle of a registration — from initial registration through check-in, cancellation, or no-show marking. This module is the primary consumer of workshop capacity and serves as the bridge between workshops and end users.

---

## Module Boundaries

```
registration/
├── RegistrationExposeAPI.java              # Public service interface (empty — TODO)
├── RegistrationNotFoundException.java      # Public exception (importable by other modules)
├── RegistrationEvents.java                 # Public event namespace (sealed interface)
├── package-info.java                       # @ApplicationModule(allowedDependencies = {"workshop"})
│
├── dto/                                    # PUBLIC DTOs (exposed via @NamedInterface)
│   ├── RegistrationRequest.java            # Public input DTO with Jakarta Validation
│   ├── RegistrationResponse.java           # Public output DTO
│   └── package-info.java                   # @NamedInterface
│
└── internal/                               # Black-box zone (ALL package-private, FLAT)
    ├── RegistrationExposeAPIImpl.java      # Implements RegistrationExposeAPI (delegates to RegistrationService)
    ├── RegistrationService.java            # Internal service interface (full CRUD, package-private)
    ├── RegistrationServiceImpl.java        # Business logic + event publishing
    ├── Registration.java                   # @Entity (JPA) with business logic
    ├── RegistrationRepository.java         # Spring Data JPA interface (extends JpaRepository)
    ├── RegistrationController.java         # REST endpoints (injects RegistrationService)
    ├── RegistrationControllerAdvice.java   # Error handling
    └── RegistrationExceptions.java         # Consolidated exceptions (static inner classes)
```

### Public API (Module Root + dto/)

Four types are exposed to other modules:

| Type | Description |
|------|-------------|
| `RegistrationExposeAPI` | Public service interface (empty — pending curated API method selection) |
| `RegistrationEvents` | Public event namespace with sealed interface and 4 event records |
| `RegistrationNotFoundException` | Public exception thrown when a registration is not found by ID |
| `RegistrationRequest` | Public input DTO (workshopId, userId) with Jakarta Validation |

The `dto/` package is exposed to other modules via `@NamedInterface` on `dto/package-info.java`.

### Cross-Module Dependencies

| Dependency | Direction | Purpose |
|------------|-----------|---------|
| `workshop` | Registration -> Workshop | Workshop validation and state checks (TODO: via WorkshopExposeAPI) |

Declared via `@ApplicationModule(allowedDependencies = {"workshop"})` in `package-info.java`.

**Note:** Registration has **no direct dependency on Room**. Room data is accessed indirectly through Workshop's `roomDisplayNameSnapshot`.

---

## One Entity = One Class

The `Registration` entity is both the domain model and the JPA persistence class. There are **no separate JPA entity classes, no mapper classes, and no repository implementation classes**.

- `Registration.java` carries `@Entity`, `@Id`, `@Column` annotations directly
- `RegistrationRepository` extends `JpaRepository<Registration, UUID>` directly
- Service constructs response DTOs inline from entity getters (no mapper)
- Package-private constructors prevent instantiation outside the module

---

## State Machine

```
                    +------------+
                    | CONFIRMED  |
                    +-----+------+
                          |
            +-------------+-------------+-------------+
            |             |             |             |
        cancel()     checkIn()     markNoShow()     (future)
            |             |             |
            v             v             v
      +-----------+  +-----------+  +-----------+
      | CANCELLED |  | ATTENDED  |  |  NO_SHOW  |
      +-----------+  +-----------+  +-----------+
       (terminal)     (terminal)     (terminal)
```

### Valid State Transitions

| Current State | Target State | Guard Conditions |
|---------------|--------------|------------------|
| CONFIRMED | CANCELLED | Unconditional (cannot cancel already-cancelled) |
| CONFIRMED | ATTENDED | Sets `checkedIn = true`, records `checkedInAt` and `checkedInBy` |
| CONFIRMED | NO_SHOW | Unconditional |
| CANCELLED | * | Terminal — no outgoing transitions |
| ATTENDED | * | Terminal — no outgoing transitions |
| NO_SHOW | * | Terminal — no outgoing transitions |

All invalid transitions throw `InvalidRegistrationStateException`.

---

## Domain Events

Four domain events are published via `ApplicationEventPublisher` within `@Transactional` context. All events are consolidated into the `RegistrationEvents` namespace at the module root.

| # | Event | Trigger | Key Payload |
|---|-------|---------|-------------|
| 1 | `RegistrationEvents.Registered` | `register()` succeeds | registrationId, workshopId, userId, occurredAt |
| 2 | `RegistrationEvents.Cancelled` | `cancel()` succeeds | registrationId, workshopId, userId, reason, occurredAt |
| 3 | `RegistrationEvents.Attended` | `checkIn()` succeeds | registrationId, workshopId, userId, checkedInBy, occurredAt |
| 4 | `RegistrationEvents.NoShow` | `markNoShow()` succeeds | registrationId, workshopId, userId, occurredAt |

Events are consumed by other modules via `@ApplicationModuleListener`.

---

## API Endpoints

All endpoints are prefixed with `/api/v1/registrations`.

| # | Method | Path | Description | Request Body / Params |
|---|--------|------|-------------|----------------------|
| 1 | `POST` | `/api/v1/registrations` | Register a user for a workshop | `RegistrationRequest` (workshopId, userId) |
| 2 | `PATCH` | `/api/v1/registrations/{id}/cancel` | Cancel a registration | None |
| 3 | `PATCH` | `/api/v1/registrations/{id}/check-in` | Check in a registered user | Query param: `checkedInBy` (UUID) |
| 4 | `PATCH` | `/api/v1/registrations/{id}/no-show` | Mark registration as no-show | None |
| 5 | `GET` | `/api/v1/registrations/workshop/{workshopId}` | List all registrations for a workshop | None |
| 6 | `GET` | `/api/v1/registrations/user/{userId}` | List all registrations for a user | None |
| 7 | `GET` | `/api/v1/registrations/workshop/{workshopId}/attendance` | Get attendance count for a workshop | None |

### Validation

- `@Valid` is applied at the **Controller layer** only. Services receive pre-validated DTOs.
- `RegistrationRequest` enforces: `workshopId` is `@NotNull`, `userId` is `@NotNull`.

### Response Format

All endpoints return `RegistrationResponse`:

```java
record RegistrationResponse(
    UUID id,
    UUID workshopId,
    UUID userId,
    String status,           // "CONFIRMED", "CANCELLED", "ATTENDED", "NO_SHOW"
    Instant registrationTime,
    boolean checkedIn,
    Instant checkedInAt,
    UUID checkedInBy,
    Instant createdAt,
    Instant updatedAt
)
```

---

## Consolidated Internal Exceptions

All internal exceptions are consolidated into a single file `RegistrationExceptions.java` as a `final` class with a private constructor, containing package-private static inner classes:

| Inner Class | Purpose |
|-------------|---------|
| `DuplicateRegistrationException` | User already registered for this workshop |
| `InvalidRegistrationStateException` | Invalid state transition (e.g., cancel already-cancelled) |
| `CapacityExceededException` | Workshop has reached maximum capacity (reserved for future use) |

---

## Database Schema

Table: `registrations`

| Column | Type | Notes |
|--------|------|-------|
| `id` | `UUID` | Primary key |
| `workshop_id` | `UUID` | Not null, FK → workshops(id) |
| `user_id` | `UUID` | Not null |
| `status` | `VARCHAR(50)` | Not null — CONFIRMED, CANCELLED, ATTENDED, NO_SHOW |
| `registration_time` | `TIMESTAMP WITH TIME ZONE` | Not null |
| `checked_in` | `BOOLEAN` | Default false |
| `checked_in_at` | `TIMESTAMP WITH TIME ZONE` | Nullable |
| `checked_in_by` | `UUID` | Nullable |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | Not null, not updatable |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | Not null |

**Indexes:**
- `idx_registrations_workshop_id` on `workshop_id`
- `idx_registrations_user_id` on `user_id`
- `idx_registrations_status` on `status`
- `idx_registrations_checked_in` on `checked_in`
- `uk_registrations_workshop_user` unique constraint on `(workshop_id, user_id)`

---

## Architecture Decisions

### RegistrationResponse Uses `String status` (Not Enum)

`RegistrationResponse` exposes `status` as a `String` rather than the `Registration.Status` enum. This follows the same pattern as `WorkshopResponse` and `RoomResponse` — prevents internal domain enum leakage to API consumers.

### No Direct Room Dependency

Registration depends only on the Workshop module. Room data is accessed indirectly through Workshop's `roomDisplayNameSnapshot`. This keeps the dependency graph clean: `Registration → Workshop → Room`.

### Event Namespace at Module Root

All 4 domain events are consolidated into `RegistrationEvents.java` at the module root using a sealed interface and records. This follows the established pattern from Workshop and Room modules.

---

## Known TODOs

- [ ] `RegistrationExposeAPI` is currently empty — needs curated public methods for cross-module integration.
- [ ] `register()` does not validate workshop state — should call `WorkshopExposeAPI` to verify workshop exists and is PUBLISHED.
- [ ] `register()` does not check capacity — should call `WorkshopExposeAPI` to verify capacity not exceeded.
- [ ] `CapacityExceededException` is defined but not yet thrown — will be used once capacity checks are implemented.
- [ ] Workshop `actualAttendance` in `WorkshopSnapshot` is initialized to 0 — should be updated from Registration attendance events.
- [ ] `cancel()` uses hardcoded reason `"Cancelled by user"` — future: accept reason parameter from controller.
