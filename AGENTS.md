# AI Coding Assistant Guidelines for Spring Boot 4 Modulith + Java 25

This document outlines the architectural principles and guidelines for AI coding assistants contributing to this project. Adhering to these guidelines ensures consistency, maintainability, and alignment with our established architectural patterns.

## Architecture Principles

### Spring Boot 4 Modulith
The project is structured using Spring Modulith, promoting a modular and cohesive design. Each module should represent a distinct bounded context or functional area.

### Facade & Black-Box Module Design (Plan B3 — Flat internal/)

Every module follows a strict two-zone package layout:

- **Public Facade (Module Root + `dto/`):** Contains only types that other modules are allowed to import: public interfaces, DTOs (Java records), event namespaces, and activation requests. DTOs reside in the `dto/` sub-package and are exposed to other modules via `@NamedInterface` on `dto/package-info.java`.
- **Black-Box (`internal/`):** Contains everything else: controllers, controller advice, service implementations, JPA entities, Spring Data repositories, and all internal exceptions — **ALL in a single flat package** with NO sub-packages. Module-level isolation is enforced by Spring Modulith `verify()`, not Java access modifiers. Every class in `internal/` is package-private.

#### Flat internal/ Encapsulation Rules

| Location | Purpose | Access |
|----------|---------|--------|
| `{module}/` (root) | Public interfaces (`*Service.java`), event namespaces (`*Events.java`), `package-info.java`, public `*NotFoundException` classes | `public` |
| `{module}/dto/` | Public request/response DTOs (Java records), activation requests | `public` |
| `{module}/internal/` | **ALL** internal classes: Entity, Repository, ServiceImpl, Controller, ControllerAdvice, internal exceptions | `package-private` |

**Hard Rules:**

1. **`dto/` is PUBLIC facade.** The `dto/` package at module root contains all public DTOs and **MUST** be exposed via `@NamedInterface` on `dto/package-info.java` so other modules can import them.
2. **`internal/` is FLAT — NO sub-packages.** Entity, Repository, ServiceImpl, Controller, ControllerAdvice, and ALL internal exceptions go directly in `internal/` root. Do NOT create `internal/web/`, `internal/exception/`, `internal/model/`, or any other sub-package.
3. **ALL classes in `internal/` are package-private.** Every single class — Entity, Repository, ServiceImpl, Controller, ControllerAdvice, and ALL exceptions — must be package-private (no `public` keyword). The review-agent will VETO any class under `internal/` that declares `public` access.
4. **Exception Consolidated File.** All internal exceptions for a module **MUST** be consolidated into a **single file** (e.g., `RoomExceptions.java`, `WorkshopExceptions.java`) as static inner classes. The outer class is `final` with a private constructor. All inner exception classes are package-private.
5. **Public exceptions live at module root.** `*NotFoundException` classes that other modules need to import MUST be `public` at the module root, NOT in `internal/`. These are the only exceptions outside `internal/`.
6. **Controller and ControllerAdvice are together in `internal/` root.** No `internal/web/` sub-package exists. Controllers and their advice classes sit alongside entities and services.

```
com.example.itworkshopticketbookingplatform/    # Main package (@SpringBootApplication)
├── ItWorkshopTicketBookingPlatformApplication.java
│
├── room/                                       # MODULE ROOM (PUBLIC FACADE)
│   ├── RoomService.java                        # Public Interface for cross-module calls
│   ├── RoomNotFoundException.java              # PUBLIC exception (module root, importable by others)
│   ├── package-info.java                       # Module metadata with allowedDependencies
│   │
│   ├── dto/                                    # PUBLIC DTOs (exposed via @NamedInterface)
│   │   ├── RoomRequest.java                    # Public Request DTO (Java Record)
│   │   ├── RoomResponse.java                   # Public Response DTO (Java Record)
│   │   └── RoomActivationRequest.java          # Public Request DTO (Java Record)
│   │
│   └── internal/                               # BLACK-BOX ZONE (ALL package-private, FLAT)
│       ├── Room.java                           # @Entity (JPA) with business logic
│       ├── RoomRepository.java                 # Spring Data JPA interface (extends JpaRepository)
│       ├── RoomServiceImpl.java                # Business logic implementation
│       ├── RoomController.java                 # Web controller (package-private)
│       ├── RoomControllerAdvice.java           # Controller advice (package-private)
│       └── RoomExceptions.java                 # Consolidated exceptions (static inner classes)
│
└── workshop/                                   # MODULE WORKSHOP (PUBLIC FACADE)
    ├── WorkshopService.java                    # Public Interface
    ├── WorkshopEvents.java                     # Public Event Namespace
    ├── WorkshopNotFoundException.java          # PUBLIC exception (module root, importable by others)
    ├── package-info.java                       # Module metadata
    │
    ├── dto/                                    # PUBLIC DTOs (exposed via @NamedInterface)
    │   ├── WorkshopRequest.java                # Public Request DTO (Java Record)
    │   └── WorkshopResponse.java               # Public Response DTO (Java Record)
    │
    └── internal/                               # BLACK-BOX ZONE (ALL package-private, FLAT)
        ├── Workshop.java                       # @Entity (JPA) with business logic
        ├── WorkshopState.java                  # Enum
        ├── WorkshopRepository.java             # Spring Data JPA interface (extends JpaRepository)
        ├── WorkshopServiceImpl.java            # Business logic implementation
        ├── WorkshopController.java             # Web controller (package-private)
        ├── WorkshopControllerAdvice.java       # Controller advice (package-private)
        └── WorkshopExceptions.java             # Consolidated exceptions (static inner classes)
```

## One Entity = One Class

Every domain entity is also its JPA persistence class. There are **no separate JPA entity classes, no mapper classes, and no repository implementation classes**.

### What This Means

| Old Pattern (Eliminated) | New Pattern (Current) |
|--------------------------|-----------------------|
| `Room.java` (domain model) | `Room.java` (domain + @Entity) |
| `RoomJpaEntity.java` (JPA entity) | *deleted* |
| `RoomMapper.java` | *deleted* |
| `RoomRepositoryImpl.java` | *deleted* |
| `RoomJpaRepository.java` | *deleted* |
| `RoomRepository.java` (domain interface) | `Room.java` is directly managed by Spring Data |

### Why

- Eliminates mapping boilerplate between parallel class hierarchies
- Removes 4+ files per module with zero behavioral difference
- Entities enforce invariants directly; Spring Data JPA handles persistence
- Services inline DTO construction from entity getters

### Rules

1. **One class per entity.** The entity class carries `@Entity`, `@Id`, `@Column`, and business logic methods.
2. **Spring Data JPA repository** extends `JpaRepository<EntityType, UUID>` and resides in `internal/`.
3. **No mapper classes.** Services construct response DTOs inline from entity getters.
4. **No repository implementation classes.** Spring Data JPA auto-generates implementations.
5. **Package-private constructors** for persistence reconstruction (JPA no-args + package-private full-args) prevent instantiation outside the module.

## Module Metadata - package-info.java

Each module explicitly declares its upstream dependencies and public DTO package via `package-info.java` files:

**Module root `package-info.java`** declares module dependencies:
```java
// room/package-info.java
@org.springframework.modulith.ApplicationModule(allowedDependencies = {})
package com.example.itworkshopticketbookingplatform.room;
```

```java
// workshop/package-info.java
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"room"})
package com.example.itworkshopticketbookingplatform.workshop;
```

**DTO sub-package `package-info.java`** marks the package as a public named interface:

```java
// room/dto/package-info.java
@org.springframework.modulith.NamedInterface
package com.example.itworkshopticketbookingplatform.room.dto;
```

```java
// workshop/dto/package-info.java
@org.springframework.modulith.NamedInterface
package com.example.itworkshopticketbookingplatform.workshop.dto;
```

**Rules:**
- `@NamedInterface` on `dto/package-info.java` exposes the DTO package to other modules (Spring Modulith 2.1+ replaces the old `apiPackage` attribute).
- `allowedDependencies = {}` (default) implies absolute isolation (zero outbound dependencies).
- Only explicitly whitelisted modules can be accessed.
- Cyclic dependencies are strictly forbidden and will fail the build.

## Event Namespace Pattern

To avoid cluttering the module's public facade with dozens of individual event files, all public events **MUST** be consolidated into a single `final class` namespace using Java `sealed interfaces` and `records`:

```java
// workshop/WorkshopEvents.java - Placed at module root (Public Facade)
package com.example.itworkshopticketbookingplatform.workshop;

import java.time.Instant;
import java.util.UUID;

public final class WorkshopEvents {
    private WorkshopEvents() {} // Prevent instantiation

    // Root interface enforcing core attributes across all workshop events
    public sealed interface WorkshopEvent permits Published, Started, Completed, Cancelled, Rescheduled, RoomChanged {
        UUID workshopId();
        Instant occurredAt();
    }

    public record Published(UUID workshopId, String title, Instant startTime, Instant endTime,
                            int capacity, UUID roomId, String roomDisplayNameSnapshot,
                            Instant occurredAt) implements WorkshopEvent {}

    public record Started(UUID workshopId, Instant occurredAt) implements WorkshopEvent {}
    public record Completed(UUID workshopId, Instant occurredAt) implements WorkshopEvent {}
    public record Cancelled(UUID workshopId, String reason, Instant occurredAt) implements WorkshopEvent {}

    public record Rescheduled(UUID workshopId, Instant oldStartTime, Instant newStartTime,
                              Instant oldEndTime, Instant newEndTime, boolean roomIdChanged,
                              String newRoomDisplayName, Instant occurredAt) implements WorkshopEvent {}

    public record RoomChanged(UUID workshopId, UUID oldRoomId, UUID newRoomId,
                              String newRoomDisplayName, Instant occurredAt) implements WorkshopEvent {}
}
```

## Event-Driven Communication

Modules **MUST** communicate asynchronously via events, **NEVER** through direct cross-module bean injection.

### Publishing Events

```java
@Service
class WorkshopServiceImpl implements WorkshopService {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public WorkshopResponse publish(String workshopId) {
        Workshop workshop = /* ... */;
        Workshop saved = workshopRepository.save(workshop);

        eventPublisher.publishEvent(new WorkshopEvents.Published(
                saved.getId(),
                saved.getTitle(),
                saved.getStartTime(),
                saved.getEndTime(),
                saved.getCapacity(),
                saved.getRoomId(),
                saved.getRoomDisplayNameSnapshot(),
                Instant.now()
        ));
        return new WorkshopResponse(saved.getId(), saved.getTitle(), saved.getDescription(),
                saved.getRoomId(), saved.getRoomDisplayNameSnapshot(), saved.getStartTime(),
                saved.getEndTime(), saved.getCapacity(), saved.getState().name(),
                saved.getCreatedAt(), saved.getUpdatedAt());
    }
}
```

### Handling Events with @ApplicationModuleListener

```java
@Service
class NotificationService {
    @ApplicationModuleListener
    public void handleWorkshopPublished(WorkshopEvents.Published event) {
        // Automatically runs in a separate thread and a separate transaction
        // Only triggers after the publisher's transaction has successfully committed
        notificationRepository.sendWorkshopPublishedNotification(event.workshopId());
    }
}
```

**`@ApplicationModuleListener` Behavior:**
- **Asynchronous Execution:** Runs out-of-band using an application thread pool.
- **Isolated Transaction:** Operates under `REQUIRES_NEW` transaction propagation.
- **Transactional Safety:** Only triggers *after* the publisher's transaction has successfully committed.
- **Consistency Model:** Enforces **Eventual Consistency** across boundaries.

## Event Publication Registry - Resiliency

Production systems **MUST** back the event pipeline with a persistent registry to guarantee at-least-once delivery:

```xml
<dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-starter-jdbc</artifactId>
</dependency>
```

Events are automatically written to an `event_publication` table within the same business transaction. Dead or failed listener transactions are automatically retried.

## Runtime Verification - CI Enforced

An architecture verification test **MUST** be maintained to immediately break the CI pipeline upon boundary violations:

```java
@SpringBootTest
class ModularityTest {
    static ApplicationModules modules = ApplicationModules.of(ItWorkshopTicketBookingPlatformApplication.class);

    @Test
    void verifyModularStructure() {
        modules.verify(); // Fails if any code breaks modular constraints
    }
}
```

**Violations caught by `verify()`:**
- **Circular Dependencies:** Detects and blocks `Module A -> Module B -> Module A` loops.
- **Internal Package Leaks:** Throws errors if any class attempts to import from another module's `internal` package.
- **Whitelist Bypasses:** Catches imports from modules not explicitly declared in `allowedDependencies`.

## Module Testing - @ApplicationModuleTest

Test modules in isolation without bootstrapping the entire application context:

```java
@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.HEADLESS)
class WorkshopModuleTest {
    @Test
    void verifyWorkshopFlow(ApplicationModuleTest.TestModule module) {
        // Loads ONLY the workshop module infrastructure. All other modules are auto-mocked.
    }
}
```

## Guidelines for AI When Generating Code

### DTOs for Public API
- **Always use DTOs for service input and output.** Services should not directly expose or return domain entities.
- **DTOs should be Java records** (immutable by default).
- **Public DTOs go at module root** (e.g., `WorkshopRequest`, `WorkshopResponse`). Internal DTOs used only within `internal/` may reside in `internal/`.
- **Response DTOs use `String` for state fields**, not internal enums, to prevent domain leakage.
- **Services construct DTOs inline** from entity getters. No mapper classes.

### DTO Validations
- **Implement format validation for DTOs.** Use standard Java Bean Validation (`jakarta.validation`) annotations (e.g., `@NotNull`, `@Size`, `@Pattern`) on DTO fields.
- **Apply DTO validations at the entry point** (e.g., in controllers or at the facade interface boundary).

### Entity Invariant Validations
- **Entities should enforce their own invariants.** These are simple, self-contained rules that must always hold true (e.g., a `Room` must always have `physicalCapacity > 0`).
- **Invariant validations should be performed within the entity's constructor or setter methods.**
- **Complex business rules** involving multiple entities, external services, or complex logic should reside in application services, not directly within domain entities.

### Package-Private Access in internal/
- **All classes in `internal/` MUST use package-private access** (no `public` keyword on the class declaration).
- **Constructors for persistence reconstruction should be package-private** to prevent instantiation outside the module.
- Only the public facade types at module root should be `public`.
- **`internal/` root** — ALL classes live here: Entity, Repository, ServiceImpl, Controller, ControllerAdvice, consolidated exceptions. All package-private.
- **Enforcement:** The review-agent will VETO any class under `internal/` that declares `public` access.

## Allowed Patterns

- **Entities as @Entity classes**: Domain entities carry JPA annotations directly. No separate JPA entity classes.
- **Spring Data JPA repositories**: Interfaces extending `JpaRepository` in `internal/`. No custom implementation classes needed.
- **Inline DTO construction**: Services build response DTOs directly from entity getters. No mapper classes.
- **Event Namespace at module root**: All public events consolidated into a single `{Module}Events.java` file at the module root using sealed interfaces and records.

## Forbidden Patterns

- **Do Not Return Entities from Services**: Application services must never directly return domain entities to the presentation layer or other modules. Always convert to a DTO.
- **Do Not Put Business Validation in Entities**: Complex business rules that involve multiple entities, external services, or complex logic should reside in application services, not directly within domain entities. Entities should only enforce their own, simple invariants.
- **No Direct Cross-Module Bean Injection**: Modules MUST NOT autowire beans from other modules directly (e.g., `@Autowired WorkshopService` inside `RoomService`). Use events for cross-module communication.
- **No Internal Package Leaks**: Classes in `internal/` are not accessible from outside the module. Never import from another module's `internal/` package.
- **No `public` Classes in internal/**: All classes under `internal/` must use package-private access to enforce compile-time encapsulation.
- **No Scattered Event Files**: Do not create individual event files under `internal/domain/event/`. Consolidate all public events into a single `{Module}Events.java` namespace at the module root.
- **No Shared Domain Packages**: Each module must own and manage its own private domain models and tables. Do not create shared 'common' packages containing database entities.
- **No Strong Consistency Across Boundaries**: Do not force multi-module actions into a single shared transaction block. Use eventual consistency via events.
- **No Separate JPA Entities**: Never create `*JpaEntity.java` classes. The domain entity IS the JPA entity.
- **No Mapper Classes**: Never create `*Mapper.java` classes. Services construct DTOs inline.
- **No Repository Implementation Classes**: Never create `*RepositoryImpl.java` classes. Spring Data JPA auto-generates implementations.
- **No Domain Repository Interfaces**: Never create domain-layer repository interfaces. Use Spring Data JPA interfaces directly in `internal/`.
- **No Value Object IDs**: Never create `*Id.java` wrapper records for entity IDs. Use `UUID` directly.

## Module Pattern Reference

The **Room** and **Workshop** modules serve as reference implementations:

- **Room module** (zero dependencies): `package-info.java` with no `allowedDependencies`. Public facade: `RoomService`, `RoomRequest`, `RoomResponse`, `RoomActivationRequest`.
- **Workshop module** (depends on `room`): `package-info.java` with `allowedDependencies = {"room"}`. Public facade: `WorkshopService`, `WorkshopRequest`, `WorkshopResponse`, `WorkshopEvents`.

For detailed module documentation, see:
- `docs/modules/workshop/README.md` - Workshop Module (Core Domain)
