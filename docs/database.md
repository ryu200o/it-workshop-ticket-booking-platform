# Database Design for IT Workshop Ticket Booking Platform

This document details the database schema, table definitions, indexes, constraints, and design considerations for the IT Workshop Ticket Booking Platform, built on PostgreSQL 17.

## General Principles

*   **UUIDs for Primary Keys**: All primary keys are universally unique identifiers (UUIDs) to facilitate modular databases and prevent reliance on sequential ID coordination.
*   **Timezone-Aware Timestamps**: All timestamp columns use `TIMESTAMP WITH TIME ZONE` (UTC) to ensure correct timezone handling across different server regions and client-server interactions.
*   **Logical References (Zero Cross-Module Foreign Keys)**: To respect Spring Modulith's module boundaries, relationships between tables of different modules are logical references using UUIDs. No physical database-level foreign key constraints are established across modules.
*   **Flyway for Migrations**: Flyway is the single source of truth for the database schema. Hibernate's `ddl-auto` is set to `validate` in local development to ensure the Java entities match the Flyway schema exactly.

---

## Table Definitions

### 1. `users` Table (Identity Module)

Stores user account credentials, profile information, and system roles.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Unique identifier for the user. Generated using UUIDv7 or UUIDv4. |
| `email` | `VARCHAR(255)` | `NOT NULL`, `UNIQUE` | User email address. Must be unique. |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` | BCrypt encrypted password hash. |
| `first_name` | `VARCHAR(255)` | `NOT NULL` | First name of the user. |
| `last_name` | `VARCHAR(255)` | `NOT NULL` | Last name of the user. |
| `role` | `VARCHAR(50)` | `NOT NULL` | System role (e.g., `ADMIN`, `ORGANIZER`, `ATTENDEE`). |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Record creation timestamp. |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Record last update timestamp. |

**Indexes & Constraints**:
*   `uk_users_email`: Unique constraint on `email`.
*   `chk_users_email_lowercase`: CHECK constraint `CHECK (email = LOWER(email))` to prevent duplicate case-variant registrations (e.g. `User@example.com` and `user@example.com`).

---

### 2. `rooms` Table (Room Module)

Manages physical locations/venues where workshops are held.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Unique identifier for the room. |
| `name` | `VARCHAR(255)` | `NOT NULL`, `UNIQUE` | Distinct room name (e.g., "Auditorium A"). |
| `capacity` | `INTEGER` | `NOT NULL`, `CHECK (capacity > 0)` | Maximum capacity of the room. |
| `location` | `VARCHAR(255)` | `NOT NULL` | Physical location (e.g., "Building B, Floor 3"). |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Record creation timestamp. |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Record last update timestamp. |

**Indexes & Constraints**:
*   `uk_rooms_name`: Unique constraint on `name`.
*   `chk_rooms_capacity`: CHECK constraint to ensure capacity is greater than 0.

---

### 3. `workshops` Table (Workshop Module)

Manages the catalog of workshops, speaker details, timing, and room snapshots.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Unique identifier for the workshop session. |
| `title` | `VARCHAR(255)` | `NOT NULL` | Title of the workshop. |
| `description` | `TEXT` | | Detailed overview of the workshop. |
| `speaker_name` | `VARCHAR(255)` | `NOT NULL` | Full name of the speaker. |
| `start_time` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Scheduled start time (UTC). |
| `end_time` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Scheduled end time (UTC). |
| `status` | `VARCHAR(50)` | `NOT NULL` | Workshop status (e.g., `SCHEDULED`, `CANCELLED`). |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Record creation timestamp. |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Record last update timestamp. |
| `room_id` | `UUID` | `NOT NULL` | Logical reference to the scheduled room (no physical foreign key). |
| `room_name` | `VARCHAR(255)` | `NOT NULL` | **Snapshot** of the room name at scheduling time. |
| `room_capacity` | `INTEGER` | `NOT NULL` | **Snapshot** of the room capacity at scheduling time (used as max workshop capacity). |
| `room_location` | `VARCHAR(255)` | `NOT NULL` | **Snapshot** of the room location at scheduling time. |

**Indexes & Constraints**:
*   `chk_workshops_time`: CHECK constraint to ensure `end_time > start_time`.
*   `idx_workshops_room_id`: Index on `room_id` to speed up room utilization queries.

---

### 4. `registrations` Table (Registration Module)

Records user ticket bookings for workshops, including aggregate-level attendance tracking.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Unique identifier for the registration. |
| `workshop_id` | `UUID` | `NOT NULL` | Logical reference to the registered workshop (no physical foreign key). |
| `user_id` | `UUID` | `NOT NULL` | Logical reference to the registering user (no physical foreign key). |
| `status` | `VARCHAR(50)` | `NOT NULL` | Registration status (e.g., `CONFIRMED`, `CANCELLED`). |
| `registration_time` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Timestamp when the booking was made. |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Record creation timestamp. |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Record last update timestamp. |
| `attended` | `BOOLEAN` | `DEFAULT FALSE` | **Embedded Value Object**: True if the user attended the workshop. |
| `attendance_marked_at` | `TIMESTAMP WITH TIME ZONE` | | **Embedded Value Object**: Timestamp when attendance was marked. |
| `attendance_marked_by` | `UUID` | | **Embedded Value Object**: Logical reference to the user who marked attendance. |

**Indexes & Constraints**:
*   `uk_registrations_workshop_user`: Unique constraint on `(workshop_id, user_id)` to prevent double booking.
*   `idx_registrations_user_id`: Index on `user_id` for quick retrieval of a user's registration history.

---

### 5. `event_publication` Table (Spring Modulith Outbox Event Registry)

Stores published domain events waiting for asynchronous outbox delivery. Managed by Spring Modulith.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Unique identifier for the event publication. |
| `listener_id` | `TEXT` | `NOT NULL` | Identifier of the consuming event listener (fully qualified class/method name). |
| `event_type` | `TEXT` | `NOT NULL` | Fully qualified class name of the published event type. |
| `serialized_event` | `TEXT` | `NOT NULL` | Serialized JSON payload of the event. |
| `publication_date` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Timestamp of event publication. |
| `completion_date` | `TIMESTAMP WITH TIME ZONE` | | Timestamp of successful consumer processing. |
| `status` | `TEXT` | | Delivery status (e.g. `PUBLISHED`). |
| `completion_attempts` | `INTEGER` | | Count of consumption retry attempts. |
| `last_resubmission_date`| `TIMESTAMP WITH TIME ZONE` | | Timestamp of the last retry attempt. |

**Indexes & Constraints**:
*   `event_publication_by_completion_date_idx`: Index on `completion_date` to quickly query uncompleted events for retry scheduling.
