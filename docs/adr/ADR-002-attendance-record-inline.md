# ADR-002: Mapping AttendanceRecord as Embedded/Inline Columns in Registrations Table

## 1. Context

The `AttendanceRecord` is modeled as a Value Object within the `Registration` Aggregate Root. This `AttendanceRecord` captures details such as `attended` (boolean), `attendance_marked_at` (timestamp), and `attendance_marked_by` (user ID). Given that each `Registration` maps to exactly one workshop (and one session), a registration can have at most one attendance status associated with it. The lifecycle of `AttendanceRecord` is entirely dependent on its parent `Registration`.

We need to decide on the persistence strategy for this `AttendanceRecord` Value Object.

## 2. Decision

We will map the fields of the `AttendanceRecord` (i.e., `attended`, `attendance_marked_at`, `attendance_marked_by`) directly as **inline columns** within the `registrations` table. This approach aligns perfectly with JPA's `@Embedded` or `@Embeddable` annotations for value objects.

## 3. Rationale

*   **High Performance**: By embedding the attendance fields directly into the `registrations` table, we eliminate the need for a separate `attendance_records` table and, consequently, any `JOIN` operations (even 1:1) when querying registration data. This leads to higher performance for read and write operations related to registration and attendance.
*   **Transactional Atomicity**: Since the `AttendanceRecord` is part of the `Registration` aggregate, embedding its fields ensures that any updates to attendance details occur within the same transaction as updates to the `Registration` itself. This guarantees transactional atomicity and maintains the integrity of the aggregate.
*   **Perfect Mapping to JPA's `@Embedded`**: This decision directly leverages JPA's `@Embedded` and `@Embeddable` annotations, which are specifically designed for persisting Value Objects as inline columns in the owning entity's table. This simplifies the ORM mapping and reduces complexity in the persistence layer.
*   **No Redundancy for 1:1 Relationship**: Given that one `Registration` always has at most one `AttendanceRecord`, creating a separate table would introduce unnecessary overhead without providing any clear benefits in terms of data normalization or flexibility.

## 4. Consequences

*   The `registrations` table will contain additional columns for attendance details (`attended`, `attendance_marked_at`, `attendance_marked_by`).
*   Data model changes related to `AttendanceRecord` will directly affect the `registrations` table schema.
*   The domain model clearly reflects that `AttendanceRecord` is an intrinsic part of `Registration` and not a standalone entity.

This decision prioritizes performance and transactional consistency for a core domain concept within the `Registration` aggregate.
