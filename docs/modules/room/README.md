# Room Module

## Purpose

The Room Module is the **Foundational Module** of the IT Workshop Ticket Booking Platform. It manages the physical rooms (lecture halls, labs, classrooms) where workshops take place. As a zero-dependency module, it forms the base layer that other modules -- such as Workshop -- depend on for room conflict validation and display name snapshots.

Rooms are exclusively **offline (in-person)** venues. Each Room represents a single physical space with a unique code, a fixed physical capacity, and a location string.

---

## Module Boundaries

```
room/
├── RoomService.java                # Public service interface
├── RoomNotFoundException.java      # Public exception (importable by other modules)
├── package-info.java               # @ApplicationModule(allowedDependencies = {})
│
├── dto/                            # PUBLIC DTOs (exposed via @NamedInterface)
│   ├── RoomRequest.java            # Public input DTO with Jakarta Validation
│   ├── RoomResponse.java           # Public output DTO
│   ├── RoomActivationRequest.java  # Public activation toggle DTO
│   └── package-info.java           # @NamedInterface
│
└── internal/                       # Black-box zone (ALL package-private, FLAT)
    ├── Room.java                   # @Entity (JPA) with business logic
    ├── RoomRepository.java         # Spring Data JPA interface (extends JpaRepository)
    ├── RoomServiceImpl.java        # Business logic implementation
    ├── RoomController.java         # REST endpoints
    ├── RoomControllerAdvice.java   # Error handling
    └── RoomExceptions.java         # Consolidated exceptions (5 static inner classes)
```

### Public API (Module Root + dto/)

Five types are exposed to other modules:

| Type | Description |
|------|-------------|
| `RoomService` | Public service interface -- the single entry point for all room operations |
| `RoomRequest` | Public input DTO (roomCode, physicalCapacity, location) with Jakarta Validation |
| `RoomResponse` | Public output DTO containing all room fields |
| `RoomActivationRequest` | Public DTO for toggling the active status of a room |
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

The Room module is a foundational module and **does not publish any domain events**. It operates as a passive reference domain -- other modules read room data but do not react to room lifecycle changes via events.

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

Room temporal fields (`createdAt`, `updatedAt`) use `java.time.LocalDateTime`. Note: the Workshop module uses `java.time.Instant` -- this inconsistency is tracked as a future alignment task.

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

## Database Schema

Table: `rooms`

| Column | Type | Notes |
|--------|------|-------|
| `id` | `UUID` | Primary key |
| `name` | `VARCHAR` | Unique, not null (maps to `roomCode`) |
| `capacity` | `INTEGER` | Not null (maps to `physicalCapacity`) |
| `location` | `VARCHAR` | Not null |
| `active` | `BOOLEAN` | Not null |
| `created_at` | `TIMESTAMP` | Not null, not updatable |
| `updated_at` | `TIMESTAMP` | Not null |

No cross-module foreign keys. The Workshop module references rooms via a logical UUID only.

---

## Testing

| Test Type | Class | Coverage |
|-----------|-------|----------|
| API Tests | `RoomControllerTests` | All 5 REST endpoints with `@Valid` verification via MockMvc, Spring REST Docs integration |

**Total: 5 tests, all passing.**

---

## Known TODOs

- Room module uses `LocalDateTime` (non-compliant with Architecture Baseline which specifies `Instant` / `TIMESTAMP WITH TIME ZONE`). This is tracked as a future alignment task.
- The Workshop module's `schedule()` uses a placeholder room display name (`"Room " + roomId`). Real implementation should call `RoomService` from the Room module to obtain the actual room code and location.
