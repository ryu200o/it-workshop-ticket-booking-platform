# ADR-003: Room Information Snapshotting inside Workshop Module

**Status: Accepted**

## 1. Context

In the IT Workshop Ticket Booking Platform, a scheduled workshop is held in a physical room. Workshops need to display room information (such as name, capacity, and location) to prospective attendees and use the room capacity to restrict the number of registrations.

We need to decide how to represent and manage room information inside the Workshop module database schema.

## 2. Decision

We will store room information as a denormalized **snapshot** within the `workshops` table. When a workshop is scheduled, the corresponding room's current state (`room_name`, `room_capacity`, and `room_location`) is copied from the Room module into the workshop record.

Furthermore, we will **NOT** implement an event-driven synchronization mechanism (such as listening to a `RoomUpdatedEvent`) to automatically propagate physical room changes to scheduled workshops.

## 3. Rationale

*   **Historical Integrity**: A workshop scheduled in the past or configured for a future date must retain the room specifications assigned at scheduling time. If Room A's capacity is physically reduced in the `rooms` catalog from 100 to 50 for a future building update, workshops already booked in that room must keep their original capacity snapshot of 100 to prevent breaking transactional invariants.
*   **Module Isolation**: Storing snapshot columns in the `workshops` table completely decouples the database schemas of the Room and Workshop modules. The Workshop module can perform its registration capacity checks and display its details without establishing cross-module database-level joins.
*   **Simple Consistency Model**: Since the registration capacity validation is performed against the workshop's snapshot, the system maintains a straightforward, synchronous consistency boundary.
*   **Avoid Over-Engineering**: Implementing `RoomUpdatedEvent` to synchronize capacity changes introduces complex questions such as how to handle already booked registrations that exceed a newly reduced room capacity (e.g., if 60 tickets are booked, and capacity is reduced to 50). Handling this requires complex business workflow decisions, manual administrative resolution, or cancellations. Avoiding automated synchronization keeps the technical design simple and production-inspired.

## 4. Consequences

*   Changes to a room's name, capacity, or location in the Room module will not automatically propagate to already scheduled workshops.
*   When creating or rescheduling a workshop, the application must query the Room module to obtain the latest details and update the workshop's snapshot.
*   Simplifies data query performance for workshops and registration validation.
