# Workshop Module

## Purpose

The Workshop Module implements the **Core Domain** of the IT Workshop Ticket Booking Platform: **University IT Workshop Management**. It is responsible for the full lifecycle of IT workshops -- from draft creation through scheduling, publishing, delivery, and completion. This module acts as the central Aggregate Root for workshop content, state management, and cross-module coordination.

Workshops are exclusively **offline (in-person)** sessions. Each Workshop represents exactly **one session** (no recurring or multi-session workshops).

---

## Module Boundaries

```
workshop/
├── WorkshopService.java              # Public service interface
├── WorkshopNotFoundException.java    # Public exception (importable by other modules)
├── WorkshopEvents.java               # Public event namespace (sealed interface)
├── package-info.java                 # @ApplicationModule(allowedDependencies = {"room"})
│
├── dto/                              # PUBLIC DTOs (exposed via @NamedInterface)
│   ├── WorkshopRequest.java          # Public input DTO with Jakarta Validation
│   ├── WorkshopResponse.java         # Public output DTO
│   └── package-info.java             # @NamedInterface
│
└── internal/                         # Black-box zone (ALL package-private, FLAT)
    ├── Workshop.java                 # @Entity (JPA) with business logic
    ├── WorkshopState.java            # State enum
    ├── WorkshopRepository.java       # Spring Data JPA interface (extends JpaRepository)
    ├── WorkshopServiceImpl.java      # Business logic + event publishing
    ├── WorkshopController.java       # REST endpoints
    ├── WorkshopControllerAdvice.java # Error handling
    ├── WorkshopPageRequest.java      # Internal DTO for pagination
    └── WorkshopExceptions.java       # Consolidated exceptions (static inner classes)
```

### Public API (Module Root)

Five types are exposed to other modules:

| Type | Description |
|------|-------------|
| `WorkshopService` | Public service interface -- the single entry point for all workshop operations |
| `WorkshopRequest` | Public input DTO (title, description) with Jakarta Validation |
| `WorkshopResponse` | Public output DTO containing all workshop fields |
| `WorkshopEvents` | Public event namespace with sealed interface and 6 event records |
| `WorkshopNotFoundException` | Public exception thrown when a workshop is not found by ID |

All other types reside under `internal/` and are not accessible from outside the module.

### Cross-Module Dependencies

| Dependency | Direction | Purpose |
|------------|-----------|---------|
| `room` | Workshop -> Room | Room conflict validation during schedule/publish/reschedule |

Declared via `@ApplicationModule(allowedDependencies = {"room"})` in `package-info.java`.

---

## One Entity = One Class

The `Workshop` entity is both the domain model and the JPA persistence class. There are **no separate JPA entity classes, no mapper classes, and no repository implementation classes**.

- `Workshop.java` carries `@Entity`, `@Id`, `@Column` annotations directly
- `WorkshopRepository` extends `JpaRepository<Workshop, UUID>` directly
- Services construct response DTOs inline from entity getters (no mapper)
- Package-private constructors prevent instantiation outside the module

---

## API Endpoints

All endpoints are prefixed with `/api/workshops`.

| # | Method | Path | Description | Request Body / Params |
|---|--------|------|-------------|----------------------|
| 1 | `POST` | `/api/workshops` | Create a new workshop in DRAFT state | `WorkshopRequest` (title, description) |
| 2 | `PUT` | `/api/workshops/{id}` | Update workshop content (title, description) | `WorkshopRequest` (title, description) |
| 3 | `PATCH` | `/api/workshops/{id}/schedule` | Schedule workshop with room, time, and capacity | Params: `startTime`, `endTime`, `capacity`, `roomId` |
| 4 | `PATCH` | `/api/workshops/{id}/publish` | Publish workshop (validates all invariants) | None |
| 5 | `PATCH` | `/api/workshops/{id}/reschedule` | Reschedule published workshop | Params: `startTime`, `endTime`, `roomId` |
| 6 | `PATCH` | `/api/workshops/{id}/start` | Start the workshop (PUBLISHED -> IN_PROGRESS) | None |
| 7 | `PATCH` | `/api/workshops/{id}/complete` | Complete the workshop (IN_PROGRESS -> COMPLETED) | None |
| 8 | `PATCH` | `/api/workshops/{id}/cancel` | Cancel the workshop (any non-terminal state) | None |
| 9 | `GET` | `/api/workshops/{id}` | Get workshop by ID | None |
| 10 | `GET` | `/api/workshops` | List all workshops (paginated) | Query params: `page`, `size`, `sort` |

### Validation

- `@Valid` is applied at the **Controller layer** only. Services receive pre-validated DTOs.
- `WorkshopRequest` enforces: `title` is `@NotBlank @Size(max=200)`, `description` is `@Size(max=2000)`.

### Response Format

All endpoints return `WorkshopResponse`:

```java
record WorkshopResponse(
    UUID id,
    String title,
    String description,
    UUID roomId,
    String roomDisplayNameSnapshot,
    Instant startTime,
    Instant endTime,
    int capacity,
    String state,            // "DRAFT", "PUBLISHED", "IN_PROGRESS", "COMPLETED", "CANCELLED"
    Instant createdAt,
    Instant updatedAt
)
```

---

## Domain Events

Six domain events are published via `ApplicationEventPublisher` within `@Transactional` context. All events are consolidated into the `WorkshopEvents` namespace at the module root using a sealed interface and records.

| # | Event | Trigger | Key Payload |
|---|-------|---------|-------------|
| 1 | `WorkshopEvents.Published` | `publish()` succeeds | workshopId, title, startTime, endTime, capacity, roomId, roomDisplayNameSnapshot, occurredAt |
| 2 | `WorkshopEvents.Rescheduled` | `reschedule()` succeeds | workshopId, oldStartTime, newStartTime, oldEndTime, newEndTime, roomIdChanged, newRoomDisplayName, occurredAt |
| 3 | `WorkshopEvents.RoomChanged` | `reschedule()` changes roomId | workshopId, oldRoomId, newRoomId, newRoomDisplayName, occurredAt |
| 4 | `WorkshopEvents.Started` | `start()` succeeds | workshopId, occurredAt |
| 5 | `WorkshopEvents.Completed` | `complete()` succeeds | workshopId, occurredAt |
| 6 | `WorkshopEvents.Cancelled` | `cancel()` succeeds | workshopId, reason, occurredAt |

Events are consumed by other modules via `@ApplicationModuleListener`.

---

## State Machine

```
                  +-----------+
                  |   DRAFT   |
                  +-----+-----+
                        |
            +-----------+-----------+
            |                       |
      publish()               cancel()
            |                       |
            v                       v
     +----------+           +-----------+
     | PUBLISHED|           | CANCELLED | <-- terminal
     +----+-----+           +-----------+
          |
    +-----+-----+
    |           |
  start()   reschedule()
    |        (stays PUBLISHED)
    v
+------------+        complete()
| IN_PROGRESS| -----------------> +-----------+
+------+------++                  | COMPLETED | <-- terminal
       |      |                   +-----------+
     cancel()
       |
       v
  +-----------+
  | CANCELLED | <-- terminal
  +-----------+
```

### Valid State Transitions

| Current State | Target State | Guard Conditions |
|---------------|--------------|------------------|
| DRAFT | PUBLISHED | All publishing invariants pass |
| DRAFT | CANCELLED | Unconditional |
| PUBLISHED | IN_PROGRESS | Unconditional |
| PUBLISHED | CANCELLED | Unconditional |
| IN_PROGRESS | COMPLETED | Unconditional |
| IN_PROGRESS | CANCELLED | Unconditional |

Terminal states (`COMPLETED`, `CANCELLED`) allow no outgoing transitions. All invalid transitions throw `InvalidWorkshopStateException`.

---

## Domain Invariants

### Publishing Invariants (all 8 must pass)

1. `roomId != null`
2. `startTime != null`
3. `endTime != null`
4. `capacity > 0`
5. `startTime < endTime`
6. `startTime` is in the future
7. No room conflict (validated via repository query against existing workshops)
8. `roomDisplayNameSnapshot` is created

### State-Driven Field Editability

| State | Editable Fields |
|-------|----------------|
| DRAFT | title, description, roomId, startTime, endTime, capacity (all via `schedule()`) |
| PUBLISHED | title, description only (via `updateContent()`); schedule/room via `reschedule()`; capacity **locked** |
| IN_PROGRESS | None (all locked) |
| COMPLETED | None (all locked) |
| CANCELLED | None (all locked) |

### Title Validation

- Must not be blank
- Maximum 200 characters

---

## Architecture Decisions

### WorkshopResponse Uses `String state` (Not Enum)

`WorkshopResponse` exposes `state` as a `String` rather than the `WorkshopState` enum. This follows the **Room module pattern** where `RoomResponse` similarly uses `String` for its state field. Rationale:
- Prevents internal domain enum leakage to API consumers
- Allows state representation changes without breaking API contracts
- Maintains consistency across modules

### One Entity = One Class

The `Workshop` entity carries JPA annotations directly. There is no separate `WorkshopJpaEntity`, no `WorkshopMapper`, and no `WorkshopRepositoryImpl`. Spring Data JPA auto-generates the repository implementation. Services construct DTOs inline from entity getters.

### Instant for All Temporal Fields

All temporal fields (`startTime`, `endTime`, `createdAt`, `updatedAt`) use `java.time.Instant`, mapping to `TIMESTAMP WITH TIME ZONE` in the database. This is timezone-aware and compliant with the Architecture Baseline v2.

### Events as Sealed Interface Namespace

All 6 domain events are consolidated into a single `WorkshopEvents.java` namespace at the module root. A sealed `WorkshopEvent` interface enforces that all events share `workshopId()` and `occurredAt()` attributes. This replaces scattered individual event files and improves discoverability.

---

## Database Schema

Table: `workshop`

| Column | Type | Notes |
|--------|------|-------|
| `id` | `UUID` | Primary key |
| `title` | `VARCHAR(200)` | Not null |
| `description` | `TEXT` | Nullable, max 2000 chars |
| `room_id` | `UUID` | Logical reference to Room (no FK constraint) |
| `room_display_name_snapshot` | `VARCHAR(200)` | Denormalized snapshot |
| `start_time` | `TIMESTAMP WITH TIME ZONE` | |
| `end_time` | `TIMESTAMP WITH TIME ZONE` | |
| `capacity` | `INTEGER` | Business capacity (0 < capacity <= Room.capacity) |
| `state` | `VARCHAR(20)` | DRAFT, PUBLISHED, IN_PROGRESS, COMPLETED, CANCELLED |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | |

No cross-module foreign keys. Room reference is a logical UUID only.

---

## Testing

| Test Type | Class | Coverage |
|-----------|-------|----------|
| Unit Tests | `WorkshopTest` | Domain model, state machine, all valid/invalid transitions, publishing invariants |
| Integration Tests | `WorkshopServiceIntegrationTests` | Service layer with mocked repository, event publishing verification |
| API Tests | `WorkshopControllerTests` | All 10 REST endpoints with `@Valid` verification via MockMvc |

**Total: 105 tests, all passing.**

---

## Known TODOs

- `schedule()` uses placeholder room display name (`"Room " + roomId`). Real implementation should call `RoomService` from the Room module.
- `cancel()` uses hardcoded reason `"Cancelled by admin"`. Future: add reason parameter to `WorkshopController.cancel()`.
- Room module uses `LocalDateTime` (non-compliant with Architecture Baseline). This is tracked as a future ADR.
