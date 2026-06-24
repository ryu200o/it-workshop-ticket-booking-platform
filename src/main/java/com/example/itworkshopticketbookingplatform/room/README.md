# Room Module

## Overview

The `room` module is responsible for managing room-related operations within the IT Workshop Ticket Booking Platform. It follows the Spring Modulith and Hexagonal Architecture principles, ensuring a clear separation of concerns, domain purity, and well-defined boundaries.

## Architecture

This module adheres to the standard module structure:

*   `api/`: Defines public contracts for inter-module communication, including DTOs and interfaces accessible by other modules.
*   `internal/`: Contains the module's internal implementation details, such as application services, domain logic, and infrastructure adapters.
*   `application/`: Orchestrates business logic and handles use cases.
*   `domain/`: Encapsulates core business logic, entities, value objects, and domain services, remaining framework-agnostic.
*   `infrastructure/`: Manages persistence (e.g., JPA entities, repositories) and external integrations.

## API

The public API of the `room` module exposes interfaces and DTOs for interaction with other modules.

### Public Interfaces

*   `RoomService` (example): Provides operations like `createRoom`, `getRoomById`, `updateRoom`, `deleteRoom`.

### DTOs

*   `RoomDto`: Data Transfer Object for exposing room information.
*   `CreateRoomCommand`: DTO for creating a new room.
*   `UpdateRoomCommand`: DTO for updating an existing room.

## Events

The `room` module may publish or consume domain events to communicate with other modules asynchronously.

### Published Events

*   `RoomCreatedEvent`: Published when a new room is successfully created.
*   `RoomUpdatedEvent`: Published when an existing room is updated.
*   `RoomDeletedEvent`: Published when a room is deleted.

### Consumed Events

*(Specify any events consumed by this module, e.g., `BookingCanceledEvent` might trigger a room availability update)*

## Dependencies

### Internal Module Dependencies

*   *(List any internal modules this module depends on, e.g., `booking` module for availability checks)*

### External Library Dependencies

*   Spring Data JPA
*   Spring Web
*   Jakarta Validation
*   MapStruct (for DTO to Entity mapping)
*   Lombok (for boilerplate code reduction)
