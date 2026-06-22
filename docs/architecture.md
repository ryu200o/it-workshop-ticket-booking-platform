# Architecture Baseline v2: Modular Monolith

The IT Workshop Ticket Booking Platform is built upon a **Modular Monolith** architectural style, leveraging **Spring Modulith** to structure the application into well-defined, independent modules. This approach aims to provide the organizational and development benefits of a modular system while maintaining the operational simplicity of a single deployable unit.

---

## 1. Architecture Roles

To ensure high engineering quality and explicit boundary management, the project defines clear responsibility divisions:

### Solution Architect (ChatGPT)
*   **Domain & Architecture Authority**: Sets the high-level architecture decisions, models, and boundaries.
*   **Technical & API Review**: Audits Pull Requests, API designs, and Aggregate Root boundaries for conformity with the baseline.

### Orchestrator (with Architecture Secretary Role)
*   **Architecture Secretary**: Responsible for maintaining the Architecture Baseline and ADR index on Linear, synchronizing architecture documents across repositories, and keeping the team in sync.
*   **Team Coordination**: Coordinates development tasks across agents and ensures that execution remains within allowed boundaries.
*   **Documentation & Sync**: Maintains the coherence between Linear, local docs, and codebase implementation.

### Engineering Team (Sub-Agents & Human Devs)
*   **Implementation**: Implements domain logic, services, database configurations, and UI components.
*   **Research**: Explores technical best practices, frameworks, and optimization paths.
*   **Internal Review**: Conducts self-tests, code formatting, and peer reviews before presenting to the reviewers.

---

## 2. Engineering Philosophy

The core principles of our engineering efforts are:

*   **Modular Boundaries**: Modules (Identity, Room, Workshop, Registration) are physically and logically segregated. Cross-module communication happens only through defined public APIs and asynchronous event publications.
*   **Zero Database-Level Coupling**: Establish **no physical cross-module foreign keys** in the database. Instead, modules store logical identifiers (UUIDs) of other modules.
*   **Timezone-Aware Accuracy**: Enforce `TIMESTAMP WITH TIME ZONE` (`Instant` / `OffsetDateTime` in Java) across all temporal fields.
*   **Data Integrity & Performance**: Utilize database-level constraints (e.g. email lowercase checks, capacity checks) to prevent invariants, and optimize logical keys with dedicated indexing.

---

## 3. Development Workflow

To maintain a production-inspired, learning-oriented, and highly traceable workspace, the team follows a strict **Linear-driven workflow**:

1.  **No Direct Commits to Main/Master**: Direct commits to protected branches are strictly forbidden.
2.  **Linear Story Intake**: All development work must start from an assigned Linear issue/story.
3.  **Branch Isolation**: For each story, a separate feature branch must be created (named after the Linear issue, e.g. `minhkhatran2k/ryu-67-ar-001...`).
4.  **Worktree Isolation**: Developers are encouraged to use a dedicated Git worktree for the branch to keep task execution completely isolated.
5.  **Stop on Conflict**: If any implementation discovers a conflict with the Architecture Baseline or ERD v1, work must **STOP** immediately. The conflict must be reported as a comment on the corresponding Linear issue, and the team must wait for an official Architecture Review before resuming.

---

## 4. Review Workflow

The pipeline from implementation to integration follows a strict quality-gate sequence:

```text
Linear Story (Task Assignment)
  ↓
Create Feature Branch (Git Branch)
  ↓
Prepare Git Worktree (Isolated Directory)
  ↓
Implementation (Implementation Phase)
  ↓
Push to GitHub (Push Phase)
  ↓
Create Pull Request (PR Stage)
  ↓
GitHub Actions (CI/CD Pipeline Verification)
  ↓
Solution Architect Review (ChatGPT Business/Domain Audit)
  ↓
Chief Engineer Review (Engineering/Technical Approval)
  ↓
Merge (Integration to main Branch)
```

---

## 5. Logical Modules

The application is logically divided into distinct modules, each responsible for a specific business capability. These modules communicate explicitly through defined interfaces, primarily through asynchronous events.

*   **Identity**: Manages user authentication, authorization, and user profiles. It is responsible for user registration, login, and access control.
*   **Room**: Handles the management of physical rooms where workshops are held. This includes room details, capacity, and location information.
*   **Workshop**: Defines and manages workshops, including their titles, descriptions, schedules, associated rooms, and capacities. It acts as the central module for workshop content.
*   **Registration**: Manages the process of users registering for workshops. This module is responsible for handling registration requests, tracking attendee status, and linking users to specific workshops.

---

## 6. Boundaries and Cross-Module Communications

Spring Modulith enforces strict module boundaries, ensuring that modules interact only through their public APIs. This promotes high cohesion within modules and loose coupling between them.

### Asynchronous Event Publications

The primary mechanism for cross-module communication is **asynchronous event publication** utilizing Spring Modulith's Event Publication Registry. When a significant domain event occurs within a module (e.g., a `WorkshopCreatedEvent` or `RegistrationCompletedEvent`), it is published. Other interested modules can subscribe to and react to these events, ensuring that business processes spanning multiple modules are handled efficiently and resiliently.

This event-driven approach provides:
*   **Loose Coupling**: Modules do not directly depend on the internal implementation details of other modules.
*   **Resilience**: Event publication and consumption can be retried, making the system more robust to transient failures.
*   **Scalability**: Event processing can be scaled independently.

### Logical References Only

Crucially, the architecture adheres to a principle of **zero physical cross-module foreign keys** in the database. Relationships between entities belonging to different modules (e.g., a `Registration` referencing a `Workshop` or a `Workshop` referencing a `Room`) are maintained through logical identifiers (e.g., UUIDs) at the application level. This prevents direct database-level coupling and allows for greater flexibility in schema evolution and potential future service extraction.

---

## 7. Domain Highlights

### Offline Workshops Only

The platform is designed exclusively for the management and booking of physical, in-person IT workshops. There is no support for online or virtual workshop sessions within the current scope.

### One Workshop = One Session

For simplicity and to align with current business requirements, each `Workshop` entity within the system represents a single, distinct session. There is no concept of multi-session workshops or recurring workshops within the current domain model.

### Registration Aggregate

The `Registration` entity is defined as an **Aggregate Root**. This means that operations affecting a registration (such as marking attendance) are encapsulated within the `Registration` aggregate, ensuring transactional consistency and upholding domain invariants. Related entities or Value Objects (like `AttendanceRecord`) are managed through the `Registration` aggregate.

### AttendanceRecord Value Object

`AttendanceRecord` is modeled as a **Value Object** within the `Registration` Aggregate Root. This signifies that an `AttendanceRecord` does not have its own identity and is intrinsically linked to the lifecycle of its owning `Registration`. It captures details such as whether an attendee was present (`attended`), when their attendance was marked (`attendance_marked_at`), and by whom (`attendance_marked_by`). This design choice simplifies persistence and ensures atomicity for attendance-related updates within a registration.
