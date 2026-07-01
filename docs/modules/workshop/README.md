# Workshop Module

## Purpose

The Workshop Module implements the **Core Domain** of the IT Workshop Ticket Booking Platform: **University IT Workshop Management**. It is responsible for the full lifecycle of IT workshops -- from draft creation through scheduling, publishing, delivery, and completion. This module acts as the central Aggregate Root for workshop content, state management, and cross-module coordination.

Workshops are exclusively **offline (in-person)** sessions. Each Workshop represents exactly **one session** (no recurring or multi-session workshops).

---

## Module Boundaries

```
workshop/
├── WorkshopExposeAPI.java                 # Public service interface
├── WorkshopNotFoundException.java         # Public exception (importable by other modules)
├── WorkshopEvents.java                    # Public event namespace (sealed interface)
├── package-info.java                      # @ApplicationModule(allowedDependencies = {"room"})
│
├── dto/                                   # PUBLIC DTOs (exposed via @NamedInterface)
│   ├── WorkshopRequest.java               # Public input DTO with Jakarta Validation
│   ├── WorkshopResponse.java              # Public output DTO
│   └── package-info.java                  # @NamedInterface
│
└── internal/                              # Black-box zone (ALL package-private, FLAT)
    ├── WorkshopExposeAPIImpl.java         # Implements WorkshopExposeAPI (delegates to WorkshopService)
    ├── WorkshopService.java               # Internal service interface (full CRUD, package-private)
    ├── WorkshopServiceImpl.java           # Business logic + history + snapshot + event publishing
    ├── Workshop.java                      # @Entity (JPA) with business logic — @Table("workshops")
    ├── WorkshopState.java                 # State enum
    ├── WorkshopRepository.java            # Spring Data JPA interface (extends JpaRepository)
    ├── WorkshopHistory.java               # @Entity (JPA) — business audit log entry
    ├── WorkshopHistoryRepository.java     # Spring Data JPA interface
    ├── WorkshopSnapshot.java              # @Entity (JPA) — immutable report (created on COMPLETED)
    ├── WorkshopSnapshotRepository.java    # Spring Data JPA interface
    ├── RoomEventHandler.java              # Handles Room events via @ApplicationModuleListener
    ├── WorkshopController.java            # REST endpoints (injects WorkshopService)
    ├── WorkshopControllerAdvice.java      # Error handling
    ├── WorkshopPageRequest.java           # Internal DTO for pagination
    └── WorkshopExceptions.java            # Consolidated exceptions (static inner classes)
```

### Public API (Module Root)

Five types are exposed to other modules:

| Type | Description |
|------|-------------|
| `WorkshopExposeAPI` | Public service interface -- the single entry point for all workshop operations |
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

### RoomEventHandler — Cross-Module Event Handling

The `RoomEventHandler` in `internal/` listens for `RoomEvents` published by the Room module using `@ApplicationModuleListener`. Each handler runs in a separate thread with `REQUIRES_NEW` transaction propagation.

| Room Event | Workshop Response | History Event Type |
|------------|-------------------|-------------------|
| `RoomRenamed` | PUBLISHED workshops: updates `roomDisplayNameSnapshot` | `ROOM_RENAMED` |
| `RoomRenamed` | IN_PROGRESS workshops: logs only | `ROOM_RENAMED_DURING_SESSION` |
| `RoomLocationChanged` | PUBLISHED workshops: logs (reschedule required) | `ROOM_LOCATION_CHANGED` |
| `RoomLocationChanged` | IN_PROGRESS workshops: logs (EMERGENCY) | `ROOM_LOCATION_CHANGED_EMERGENCY` |
| `RoomDeactivated` | PUBLISHED/IN_PROGRESS workshops: logs | `ROOM_DEACTIVATED` |

```java
@Service
class RoomEventHandler {
    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRoomRenamed(RoomEvents.RoomRenamed event) { ... }

    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRoomLocationChanged(RoomEvents.RoomLocationChanged event) { ... }

    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRoomDeactivated(RoomEvents.RoomDeactivated event) { ... }
}
```

---

## Audit History — WorkshopHistory

Every mutation to a Workshop is recorded in the `workshop_histories` table. This provides a complete business audit trail. `WorkshopServiceImpl` calls `saveHistory()` after every state-changing operation.

Event types recorded: `DRAFT_CREATED`, `CONTENT_UPDATED`, `SCHEDULED`, `PUBLISHED`, `RESCHEDULED`, `STARTED`, `COMPLETED`, `CANCELLED`, plus cross-module events from `RoomEventHandler` (`ROOM_RENAMED`, `ROOM_LOCATION_CHANGED`, `ROOM_DEACTIVATED`, etc.).

```java
@Entity
@Table(name = "workshop_histories")
class WorkshopHistory {
    UUID id;
    UUID workshopId;       // FK → workshops(id)
    String eventType;      // e.g., "PUBLISHED", "ROOM_RENAMED"
    Map<String, Object> eventData;  // JSONB — event-specific payload
    String reason;         // Human-readable reason
    UUID changedBy;        // Who made the change
    Instant occurredAt;    // When the event occurred
    Instant createdAt;     // Record creation time
}
```

---

## WorkshopSnapshot — Immutable Report

A `WorkshopSnapshot` is created once when a workshop transitions to `COMPLETED`. It captures a denormalized snapshot of the workshop's final state for reporting purposes. Snapshots are immutable — no update or delete operations.

```java
@Entity
@Table(name = "workshop_snapshots")
class WorkshopSnapshot {
    UUID id;
    UUID workshopId;       // FK → workshops(id), UNIQUE
    String roomName;       // Denormalized from roomDisplayNameSnapshot
    String roomLocation;   // Denormalized (TODO: fetch from Room module)
    Instant startTime;
    Instant endTime;
    int capacity;
    int actualAttendance;  // Default 0 (TODO: update from Registration)
    BigDecimal feedbackScore;  // Nullable
    Instant completedAt;
    Instant createdAt;
}
```

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

**Side effects on transitions:** Every state-changing operation in `WorkshopServiceImpl` now (1) saves a `WorkshopHistory` audit record, (2) publishes domain events via `ApplicationEventPublisher`, and (3) on `COMPLETED`, creates a `WorkshopSnapshot`.

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

### @Table(name = "workshops") — Plural Naming Convention

The Workshop entity uses `@Table(name = "workshops")` (plural). This aligns with the Room module (`rooms`) and Registration module (`registrations`). The table was renamed from `workshop` to `workshops` via Flyway V5.

### WorkshopHistory as Business Audit Log

`WorkshopHistory` captures lifecycle events as JSONB payloads. This is distinct from the technical `event_publication` outbox (Spring Modulith). The audit log records *what changed*; the outbox records *whether events were delivered*.

### WorkshopSnapshot as Immutable Report

`WorkshopSnapshot` is created once on COMPLETED and never modified. The unique index on `workshop_id` enforces one snapshot per workshop. This pattern is suitable for reporting and analytics without impacting the live workshop data.

### RoomEventHandler for Cross-Module Event Handling

`RoomEventHandler` uses `@ApplicationModuleListener` with `REQUIRES_NEW` transaction propagation to handle Room events in isolation. This ensures the Workshop module's transaction is independent of the Room module's transaction, maintaining proper module boundaries.

---

## Database Schema

### Table: `workshops` (renamed from `workshop` via Flyway V5)

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
| `state` | `VARCHAR(50)` | DRAFT, PUBLISHED, IN_PROGRESS, COMPLETED, CANCELLED (expanded from VARCHAR(20)) |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | |

**Indexes:** `idx_workshops_room_id` (renamed from `idx_workshop_room_id`)

### Table: `workshop_histories` (Flyway V5)

Business audit log — one record per lifecycle event (DRAFT_CREATED, CONTENT_UPDATED, SCHEDULED, PUBLISHED, RESCHEDULED, STARTED, COMPLETED, CANCELLED, plus cross-module events from RoomEventHandler).

| Column | Type | Notes |
|--------|------|-------|
| `id` | `UUID` | Primary key |
| `workshop_id` | `UUID` | Not null, FK → workshops(id) |
| `event_type` | `VARCHAR(50)` | Not null — e.g., DRAFT_CREATED, PUBLISHED, ROOM_RENAMED |
| `event_data` | `TEXT` (JSONB) | Not null — event-specific payload |
| `reason` | `VARCHAR(255)` | Nullable |
| `changed_by` | `UUID` | Not null |
| `occurred_at` | `TIMESTAMP WITH TIME ZONE` | Not null |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | Not null |

**Indexes:** `idx_workshop_histories_workshop_id`, `idx_workshop_histories_occurred_at`, `idx_workshop_histories_event_type`

### Table: `workshop_snapshots` (Flyway V5)

Immutable report — created once when a workshop transitions to COMPLETED. One snapshot per workshop (enforced by unique index).

| Column | Type | Notes |
|--------|------|-------|
| `id` | `UUID` | Primary key |
| `workshop_id` | `UUID` | Not null, FK → workshops(id), UNIQUE |
| `room_name` | `VARCHAR(255)` | Not null |
| `room_location` | `VARCHAR(255)` | Not null |
| `start_time` | `TIMESTAMP WITH TIME ZONE` | Not null |
| `end_time` | `TIMESTAMP WITH TIME ZONE` | Not null |
| `capacity` | `INT` | Not null |
| `actual_attendance` | `INT` | Default 0 |
| `feedback_score` | `DECIMAL(3,2)` | Nullable |
| `completed_at` | `TIMESTAMP WITH TIME ZONE` | Not null |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | Not null |

**Indexes:** `uk_workshop_snapshots_workshop` (unique on `workshop_id`), `idx_workshop_snapshots_completed_at`

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

- `WorkshopExposeAPI` is currently empty — needs curated public methods for cross-module integration (Registration module needs to validate workshop state/capacity).
- `schedule()` uses placeholder room display name (`"Room " + roomId`). Real implementation should call `RoomExposeAPI` from the Room module.
- `cancel()` uses hardcoded reason `"Cancelled by admin"`. Future: add reason parameter to `WorkshopController.cancel()`.
- `WorkshopSnapshot.roomLocation` is hardcoded to `"Unknown"` — should fetch actual room location from Room module.
- `WorkshopSnapshot.actualAttendance` is initialized to 0 — should be updated from Registration module attendance events.
- Room module uses `LocalDateTime` (non-compliant with Architecture Baseline). This is tracked as a future ADR.
