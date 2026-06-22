# Architecture Overview: Modular Monolith

The IT Workshop Ticket Booking Platform is built upon a **Modular Monolith** architectural style, leveraging **Spring Modulith** to structure the application into well-defined, independent modules. This approach aims to provide the organizational and development benefits of a modular system while maintaining the operational simplicity of a single deployable unit.

## Logical Modules

The application is logically divided into distinct modules, each responsible for a specific business capability. These modules communicate explicitly through defined interfaces, primarily through asynchronous events.

*   **Identity**: Manages user authentication, authorization, and user profiles. It is responsible for user registration, login, and access control.
*   **Room**: Handles the management of physical rooms where workshops are held. This includes room details, capacity, and location information.
*   **Workshop**: Defines and manages workshops, including their titles, descriptions, schedules, associated rooms, and capacities. It acts as the central module for workshop content.
*   **Registration**: Manages the process of users registering for workshops. This module is responsible for handling registration requests, tracking attendee status, and linking users to specific workshops.

## Boundaries and Cross-Module Communications

Spring Modulith enforces strict module boundaries, ensuring that modules interact only through their public APIs. This promotes high cohesion within modules and loose coupling between them.

### Asynchronous Event Publications

The primary mechanism for cross-module communication is **asynchronous event publication** utilizing Spring Modulith's Event Publication Registry. When a significant domain event occurs within a module (e.g., a `WorkshopCreatedEvent` or `RegistrationCompletedEvent`), it is published. Other interested modules can subscribe to and react to these events, ensuring that business processes spanning multiple modules are handled efficiently and resiliently.

This event-driven approach provides:
*   **Loose Coupling**: Modules do not directly depend on the internal implementation details of other modules.
*   **Resilience**: Event publication and consumption can be retried, making the system more robust to transient failures.
*   **Scalability**: Event processing can be scaled independently.

### Logical References Only

Crucially, the architecture adheres to a principle of **zero physical cross-module foreign keys** in the database. Relationships between entities belonging to different modules (e.g., a `Registration` referencing a `Workshop` or a `Workshop` referencing a `Room`) are maintained through logical identifiers (e.g., UUIDs) at the application level. This prevents direct database-level coupling and allows for greater flexibility in schema evolution and potential future service extraction.

## Domain Highlights

### Offline Workshops Only

The platform is designed exclusively for the management and booking of physical, in-person IT workshops. There is no support for online or virtual workshop sessions within the current scope.

### One Workshop = One Session

For simplicity and to align with current business requirements, each `Workshop` entity within the system represents a single, distinct session. There is no concept of multi-session workshops or recurring workshops within the current domain model.

### Registration Aggregate

The `Registration` entity is defined as an **Aggregate Root**. This means that operations affecting a registration (such as marking attendance) are encapsulated within the `Registration` aggregate, ensuring transactional consistency and upholding domain invariants. Related entities or Value Objects (like `AttendanceRecord`) are managed through the `Registration` aggregate.

### AttendanceRecord Value Object

`AttendanceRecord` is modeled as a **Value Object** within the `Registration` Aggregate Root. This signifies that an `AttendanceRecord` does not have its own identity and is intrinsically linked to the lifecycle of its owning `Registration`. It captures details such as whether an attendee was present (`attended`), when their attendance was marked (`attendance_marked_at`), and by whom (`attendance_marked_by`). This design choice simplifies persistence and ensures atomicity for attendance-related updates within a registration.
