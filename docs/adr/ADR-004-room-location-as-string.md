# ADR-004: Keeping Room Location as String in Phase 1

**Status: Proposed**

## 1. Context

The `location` of a Room is currently a simple `String`. The Solution Architect noted this as a potential code smell, suggesting a `Location` Value Object (VO) containing fields like `building`, `floor`, and `description` might be better if the business heavily relies on location details.

## 2. Decision

We will keep `location` as a `String` for Phase 1.

## 3. Rationale

For the current MVP scope, the location is merely descriptive text displayed to the user. We do not yet have complex business rules (like calculating distance, filtering by building, or managing campus resources) that justify the overhead of a dedicated Value Object and multiple database columns. Keeping it simple avoids over-engineering early in the project.

## 4. Future Plan

If future API discovery or business requirements necessitate structured location data, we will refactor the `String` into a `Location` Value Object and apply the necessary database schema migrations.
