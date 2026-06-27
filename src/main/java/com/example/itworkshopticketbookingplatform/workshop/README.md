# Workshop Module

## Overview
Workshop Module implements the Core Domain "University IT Workshop Management" as a Spring Modulith module following Hexagonal/DDD architecture.

## Public API (Module Root)

### Service Interface
```java
public interface WorkshopService {
    WorkshopResponse createDraft(WorkshopRequest request);
    WorkshopResponse updateContent(String workshopId, WorkshopRequest request);
    WorkshopResponse schedule(String workshopId, Instant startTime, Instant endTime, int capacity, String roomId);
    WorkshopResponse publish(String workshopId);
    WorkshopResponse reschedule(String workshopId, Instant startTime, Instant endTime, String roomId);
    WorkshopResponse start(String workshopId);
    WorkshopResponse complete(String workshopId);
    WorkshopResponse cancel(String workshopId);
    WorkshopResponse findById(String workshopId);
    Page<WorkshopResponse> findAll(Pageable pageable);
}
```

### Request/Response DTOs
- `WorkshopRequest` - Input for create/update operations
- `WorkshopResponse` - Output for all operations

## Domain Events (Internal - for cross-module communication)

All events are published via `ApplicationEventPublisher` and consumed via `@ApplicationModuleListener`:

| Event | Trigger | Payload |
|-------|---------|---------|
| `WorkshopPublishedEvent` | `publish()` succeeds | workshopId, title, startTime, endTime, capacity, roomId, roomDisplayNameSnapshot |
| `WorkshopRescheduledEvent` | `reschedule()` succeeds | workshopId, oldStartTime, newStartTime, oldEndTime, newEndTime, roomIdChanged, newRoomDisplayName |
| `WorkshopRoomChangedEvent` | `reschedule()` changes roomId | workshopId, oldRoomId, newRoomId, newRoomDisplayName |
| `WorkshopStartedEvent` | `start()` succeeds | workshopId, startedAt |
| `WorkshopCompletedEvent` | `complete()` succeeds | workshopId, completedAt |
| `WorkshopCancelledEvent` | `cancel()` succeeds | workshopId, cancelledAt, reason |

## State Machine

```
DRAFT → PUBLISHED → IN_PROGRESS → COMPLETED
         ↓              ↓
       CANCELLED     CANCELLED
```

## Cross-Module Dependencies

- **Room Module** (declared in `package-info.java` as `allowedDependencies = {"room"}`)
  - Used for: Room conflict validation during `schedule()` and `reschedule()`
  - Access via: `RoomService` (public API)
  - Does NOT listen to Room events (per ADR-003)

## Invariants

### Publishing (all must pass)
1. `roomId != null`
2. `startTime != null`
3. `endTime != null`
4. `capacity > 0`
5. `startTime < endTime`
6. `startTime` in future
7. No Room Conflict (via Room module)
8. `roomDisplayNameSnapshot` created successfully

### State-Driven Mutations
| State | Allowed |
|-------|---------|
| DRAFT | All fields |
| PUBLISHED | title, description; schedule/room via `reschedule()`; capacity LOCKED |
| IN_PROGRESS | NONE (all locked) |
| COMPLETED | NONE (all locked) |
| CANCELLED | NONE (all locked) |

## Internal Structure

```
workshop/
├── WorkshopService.java              # Public API
├── WorkshopRequest.java              # Public DTO
├── WorkshopResponse.java             # Public DTO
├── package-info.java                 # @ApplicationModule(allowedDependencies = {"room"})
└── internal/
    ├── application/
    │   ├── dto/                      # Internal DTOs only
    │   ├── mapper/                   # MapStruct mappers
    │   └── service/
    │       └── WorkshopServiceImpl.java
    ├── domain/
    │   ├── event/                    # Domain events (internal)
    │   ├── exception/
    │   ├── model/
    │   │   └── Workshop.java         # Aggregate Root
    │   └── repository/
    │       └── WorkshopRepository.java
    ├── infrastructure/
    │   └── persistence/
    │       ├── jpa/
    │       │   ├── WorkshopJpaEntity.java
    │       │   └── WorkshopJpaRepository.java
    │       └── repository/
    │           └── WorkshopRepositoryImpl.java
    └── presentation/
        └── controller/
            ├── WorkshopController.java
            └── WorkshopControllerAdvice.java
```

## Database

Table: `workshop`
- Uses `TIMESTAMP WITH TIME ZONE` for all temporal columns (`Instant` in Java)
- Stores `room_display_name_snapshot` denormalized

## Testing

- Unit tests: Domain model, state machine, invariants
- Integration tests: Repository, service layer with Testcontainers
- API tests: Controller endpoints with WebMvcTest
- Modulith tests: Verify module boundaries