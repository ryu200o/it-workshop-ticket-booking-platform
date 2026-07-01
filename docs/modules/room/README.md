# Room Module

## Purpose

The Room Module is the **Foundational Module** of the IT Workshop Ticket Booking Platform. It manages the physical rooms (lecture halls, labs, classrooms) where workshops take place. As a zero-dependency module, it forms the base layer that other modules -- such as Workshop -- depend on for room conflict validation and display name snapshots.

Rooms are exclusively **offline (in-person)** venues. Each Room represents a single physical space with a unique code, a fixed physical capacity, and a location string.

---

## Module Boundaries

```
room/
├── RoomExposeAPI.java                   # Public service interface
├── RoomNotFoundException.java           # Public exception (importable by other modules)
├── RoomEvents.java                      # Public event namespace (sealed interface)
├── package-info.java                    # @ApplicationModule(allowedDependencies = {})
│
├── dto/                                 # PUBLIC DTOs (exposed via @NamedInterface)
│   ├── RoomRequest.java                 # Public input DTO with Jakarta Validation
│   ├── RoomResponse.java                # Public output DTO
│   ├── RoomActivationRequest.java       # Public activation toggle DTO
│   └── package-info.java                # @NamedInterface
│
└── internal/                            # Black-box zone (ALL package-private, FLAT)
    ├── RoomExposeAPIImpl.java           # Implements RoomExposeAPI (delegates to RoomService)
    ├── RoomService.java                 # Internal service interface (full CRUD, package-private)
    ├── RoomServiceImpl.java             # Business logic + history + event publishing
    ├── Room.java                        # @Entity (JPA) with business logic
    ├── RoomRepository.java              # Spring Data JPA interface (extends JpaRepository)
    ├── RoomHistory.java                 # @Entity (JPA) — audit log entry
    ├── RoomHistoryRepository.java       # Spring Data JPA interface
    ├── RoomController.java              # REST endpoints (injects RoomService)
    ├── RoomControllerAdvice.java        # Error handling
    └── RoomExceptions.java              # Consolidated exceptions (5 static inner classes)
```

### Public API (Module Root + dto/)

Six types are exposed to other modules:

| Type | Description |
|------|-------------|
| `RoomExposeAPI` | Public service interface -- the single entry point for all room operations |
| `RoomRequest` | Public input DTO (roomCode, physicalCapacity, location) with Jakarta Validation |
| `RoomResponse` | Public output DTO containing all room fields |
| `RoomActivationRequest` | Public DTO for toggling the active status of a room |
| `RoomEvents` | Public event namespace with sealed interface and 3 event records |
| `RoomNotFoundException` | Public exception thrown when a room is not found by ID or room code |

The `dto/` package is exposed to other modules via `@NamedInterface` on `dto/package-info.java`. All other types reside under `internal/` and are not accessible from outside the module.

### Cross-Module Dependencies

| Dependency | Direction | Purpose |
|------------|-----------|---------|
| *(none)* | -- | Room is a foundational module with zero outbound dependencies |

Declared via `@ApplicationModule(allowedDependencies = {})` in `package-info.java`.

---

## One Entity = One Class

The `Room` entity is both the domain model and the JPA persistence class. There are **no separate JPA entity classes, no mapper classes, and no repository implementation classes**.

- `Room.java` carries `@Entity`, `@Id`, `@Column` annotations directly
- `RoomRepository` extends `JpaRepository<Room, UUID>` directly
- Service constructs response DTOs inline from entity getters (no mapper)
- Package-private constructors prevent instantiation outside the module

---

## API Endpoints

All endpoints are prefixed with `/api/v1/rooms`.

| # | Method | Path | Description | Request Body / Params |
|---|--------|------|-------------|----------------------|
| 1 | `POST` | `/api/v1/rooms` | Create a new room (active by default) | `RoomRequest` (roomCode, physicalCapacity, location) |
| 2 | `PUT` | `/api/v1/rooms/{roomId}` | Update room (roomCode, physicalCapacity, location) | `RoomRequest` (roomCode, physicalCapacity, location) |
| 3 | `PATCH` | `/api/v1/rooms/{roomId}/activation` | Activate or deactivate a room | `RoomActivationRequest` (active) |
| 4 | `GET` | `/api/v1/rooms` | List all rooms | None |
| 5 | `GET` | `/api/v1/rooms/{roomId}` | Get room by ID | None |

### Validation

- `@Valid` is applied at the **Controller layer** only. Services receive pre-validated DTOs.
- `RoomRequest` enforces: `roomCode` is `@NotBlank`, `physicalCapacity` is `@Positive`, `location` is `@NotBlank`.
- `RoomActivationRequest` enforces: `active` is `@NotNull`.

### Response Format

All endpoints return `RoomResponse`:

```java
record RoomResponse(
    UUID id,
    String roomCode,
    int physicalCapacity,
    String location,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
)
```

---

## Domain Events

The Room module publishes domain events via `ApplicationEventPublisher` within `@Transactional` context after mutations. All events are consolidated into the `RoomEvents` namespace at the module root using a sealed interface and records.

| # | Event | Trigger | Key Payload |
|---|-------|---------|-------------|
| 1 | `RoomEvents.RoomRenamed` | `updateRoom()` changes roomCode | roomId, oldName, newName, occurredAt |
| 2 | `RoomEvents.RoomLocationChanged` | `updateRoom()` changes location | roomId, oldLocation, newLocation, occurredAt |
| 3 | `RoomEvents.RoomDeactivated` | `activateDeactivateRoom()` sets active=false | roomId, wasActive, occurredAt |

Events are consumed by other modules via `@ApplicationModuleListener`. The Workshop module's `RoomEventHandler` listens for these events to update affected workshops.

```java
// RoomEvents namespace at module root
public final class RoomEvents {
    public sealed interface RoomEvent permits RoomRenamed, RoomLocationChanged, RoomDeactivated {
        UUID roomId();
        Instant occurredAt();
    }
    public record RoomRenamed(UUID roomId, String oldName, String newName, Instant occurredAt) implements RoomEvent {}
    public record RoomLocationChanged(UUID roomId, String oldLocation, String newLocation, Instant occurredAt) implements RoomEvent {}
    public record RoomDeactivated(UUID roomId, boolean wasActive, Instant occurredAt) implements RoomEvent {}
}
```

---

## Domain Invariants

### Entity-Level Invariants (enforced in Room constructor and setters)

1. `roomCode` must not be blank
2. `physicalCapacity` must be greater than 0
3. `location` must not be blank
4. New rooms are created with `active = true` by default
5. `createdAt` and `updatedAt` are set automatically on creation
6. `updatedAt` is refreshed on every mutation (rename, changeCapacity, changeLocation, activate, deactivate)

### Service-Level Invariants (enforced in RoomServiceImpl)

1. `createRoom`: Room code must be unique (checked via `findByRoomCode`); throws `DuplicateRoomCodeException` on conflict
2. `updateRoom`: Room must exist (throws `RoomNotFoundException`); room code uniqueness is checked only if the code actually changed
3. `activateDeactivateRoom`: Room must exist (throws `RoomNotFoundException`)
4. `getRoomDetail`: Room must exist (throws `RoomNotFoundException`)
5. `getRoomList`: Returns all rooms (no filtering)

---

## Architecture Decisions

### RoomResponse Uses `boolean active` (Not Enum)

`RoomResponse` exposes `active` as a `boolean` rather than an enum. This is a simple presence/absence flag -- no state machine is needed for a room.

### One Entity = One Class

The `Room` entity carries JPA annotations directly. There is no separate `RoomJpaEntity`, no `RoomMapper`, and no `RoomRepositoryImpl`. Spring Data JPA auto-generates the repository implementation. Services construct DTOs inline from entity getters.

### LocalDateTime for Temporal Fields

Room temporal fields (`createdAt`, `updatedAt`) use `java.time.LocalDateTime`. Note: the Workshop module uses `java.time.Instant` -- this inconsistency is tracked as a future alignment task. Note: `RoomHistory` temporal fields use `Instant` (TIMESTAMPTZ), aligning with the Workshop convention.

### RoomEvents Namespace

All 3 domain events are consolidated into a single `RoomEvents.java` namespace at the module root using a sealed interface and records. This follows the established pattern from `WorkshopEvents`. Events are published by `RoomServiceImpl` after mutations and consumed by the Workshop module's `RoomEventHandler`.

### RoomHistory Audit Log

`RoomHistory` captures field-level change diffs as JSONB (`Map<String, Object>`). The `JsonbConverter` in `shared/` handles serialization automatically via `@Converter(autoApply = true)`. History records are append-only — no updates or deletes.

### Public Exception at Module Root

`RoomNotFoundException` is placed at the module root (not under `internal/`) so that other modules can import it. All internal exceptions are consolidated into `RoomExceptions.java` under `internal/` as package-private static inner classes.

---

## Consolidated Internal Exceptions

All internal exceptions are consolidated into a single file `RoomExceptions.java` as a `final` class with a private constructor, containing package-private static inner classes:

| Inner Class | Purpose |
|-------------|---------|
| `RoomDomainException` | Generic room domain error |
| `InvalidRoomCodeException` | Room code validation failure |
| `InvalidPhysicalCapacityException` | Physical capacity out of range |
| `InvalidLocationException` | Location validation failure |
| `DuplicateRoomCodeException` | Room code already exists |

---

## Audit History — RoomHistory

Every mutation to a Room (create, update, activate/deactivate) is recorded in the `room_histories` table. This provides a complete business audit trail distinct from the technical `event_publication` outbox.

`RoomHistory` is an `@Entity` in `internal/` (package-private). It stores changes as a JSONB `Map<String, Object>` capturing field-level before/after values.

```java
@Entity
@Table(name = "room_histories")
class RoomHistory {
    UUID id;
    UUID roomId;          // FK → rooms(id)
    Instant changedAt;    // When the change occurred
    UUID changedBy;       // Who made the change (currently UUID.randomUUID())
    String reason;        // Human-readable reason
    Map<String, Object> changes;  // JSONB — field-level diffs
    Instant createdAt;    // Record creation time
}
```

`RoomServiceImpl` saves a `RoomHistory` record after every successful mutation (update, activate, deactivate). History records are append-only — no updates or deletes.

---

## Database Schema

### Table: `rooms`

| Column | Type | Notes |
|--------|------|-------|
| `id` | `UUID` | Primary key |
| `name` | `VARCHAR` | Unique, not null (maps to `roomCode`) |
| `capacity` | `INTEGER` | Not null (maps to `physicalCapacity`) |
| `location` | `VARCHAR` | Not null |
| `active` | `BOOLEAN` | Not null |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | Not null, not updatable |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | Not null |

### Table: `room_histories` (Flyway V4)

| Column | Type | Notes |
|--------|------|-------|
| `id` | `UUID` | Primary key |
| `room_id` | `UUID` | Not null, FK → rooms(id) |
| `changed_at` | `TIMESTAMP WITH TIME ZONE` | Not null |
| `changed_by` | `UUID` | Not null |
| `reason` | `VARCHAR(255)` | Nullable |
| `changes` | `TEXT` (JSONB) | Not null — field-level change diffs |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | Not null |

**Indexes:** `idx_room_histories_room_id`, `idx_room_histories_changed_at`

No cross-module foreign keys. The Workshop module references rooms via a logical UUID only.

---

## Testing

| Test Type | Class | Coverage |
|-----------|-------|----------|
| API Tests | `RoomControllerTests` | All 5 REST endpoints with `@Valid` verification via MockMvc, Spring REST Docs integration |

**Total: 5 tests, all passing.**

---

## Known TODOs

- Room entity uses `LocalDateTime` for `createdAt`/`updatedAt` (non-compliant with Architecture Baseline which specifies `Instant` / `TIMESTAMP WITH TIME ZONE`). Note: `RoomHistory` already uses `Instant`. This is tracked as a future alignment task.
- `RoomExposeAPI` is currently empty — needs curated public methods for cross-module integration (Workshop module needs to fetch room details).
- The Workshop module's `schedule()` uses a placeholder room display name (`"Room " + roomId`). Real implementation should call `RoomExposeAPI` from the Room module to obtain the actual room code and location.
