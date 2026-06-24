# AI Coding Assistant Guidelines for Spring Boot 4 Modulith + Java 25

This document outlines the architectural principles and guidelines for AI coding assistants contributing to this project. Adhering to these guidelines ensures consistency, maintainability, and alignment with our established architectural patterns.

## Architecture Principles

### Spring Boot 4 Modulith
The project is structured using Spring Modulith, promoting a modular and cohesive design. Each module should represent a distinct bounded context or functional area.

### Layer Responsibilities
- **Application Layer**: Orchestrates business logic, handles use cases, and coordinates across domain entities and infrastructure services. It defines DTOs for input and output.
- **Domain Layer**: Contains the core business logic, entities, value objects, and domain services. It enforces domain invariants.
- **Infrastructure Layer**: Provides technical capabilities like persistence, messaging, and external API integrations.

### Standard Module Structure
Each module should follow a consistent directory structure, typically including:
- `application/`: Contains application services, DTOs, mappers, and use case implementations.
- `domain/`: Contains domain entities, value objects, domain services, and repositories interfaces.
- `infrastructure/`: Contains persistence implementations (e.g., Spring Data repositories), external service clients, and message listeners/producers.
- `api/`: (Optional, for external module exposure) Contains public interfaces and DTOs for interaction with other modules or external systems.

## Guidelines for AI When Generating Code

### DTOs in Service Layer
- **Always use DTOs for service input and output.** Services should not directly expose or return domain entities. This provides a clear contract and prevents unintentional exposure of internal domain details.
- **DTOs should be immutable where possible.**

### DTO Validations
- **Implement format validation for DTOs.** Use standard Java Bean Validation (`jakarta.validation`) annotations (e.g., `@NotNull`, `@Size`, `@Pattern`) on DTO fields.
- **Apply DTO validations at the entry point of the application layer** (e.g., in controllers or application service methods).

### Entity Invariant Validations
- **Entities should enforce their own invariants.** These are rules that must always hold true for an entity to be in a valid state (e.g., a `Ticket` must always have a `price > 0`).
- **Invariant validations should be performed within the entity's constructor or setter methods.** They should be simple, self-contained checks.

## Allowed Patterns

- **Mappers in Application Layer**: Use mapping frameworks (e.g., MapStruct) to convert between DTOs and entities. Mappers should reside in the application layer.
- **DTOs in `application/dto`**: All Data Transfer Objects (DTOs) should be defined within the `application/dto` package of their respective module.
- **Repositories return Entities**: Repository interfaces (defined in the domain layer) and their implementations (in the infrastructure layer) should return domain entities.

## Forbidden Patterns

- **Do Not Return Entities from Services**: Application services must never directly return domain entities to the presentation layer or other modules. Always convert to a DTO.
- **Do Not Put Business Validation in Entities**: Complex business rules that involve multiple entities, external services, or complex logic should reside in application services or domain services, not directly within domain entities. Entities should only enforce their own, simple invariants.
- **No Direct Database Access from Application Layer**: The application layer should interact with data persistence through repository interfaces defined in the domain layer. Direct access to `EntityManager`, `JdbcTemplate`, or similar persistence technologies is forbidden.
- **No Direct Access to Infrastructure from Application Layer**: Application services should not directly depend on concrete infrastructure implementations. Dependency inversion should be used.
