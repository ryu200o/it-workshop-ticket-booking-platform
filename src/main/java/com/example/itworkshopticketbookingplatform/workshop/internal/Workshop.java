package com.example.itworkshopticketbookingplatform.workshop.internal;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workshop")
class Workshop {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "room_id", columnDefinition = "uuid")
    private UUID roomId;

    @Column(name = "room_display_name_snapshot", length = 200)
    private String roomDisplayNameSnapshot;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private WorkshopState state;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // No-args constructor for JPA
    protected Workshop() {}

    // ============ Constructors ============

    /**
     * Creates a new Workshop in DRAFT state.
     * Used by createDraft() aggregate behavior.
     */
    Workshop(String title, String description) {
        this.id = UUID.randomUUID();
        this.title = validateTitle(title);
        this.description = description;
        this.state = WorkshopState.DRAFT;
        this.capacity = 0;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Rehydrates Workshop from persistence.
     * Package-private - only infrastructure layer should use this.
     */
    Workshop(UUID id,
             String title,
             String description,
             UUID roomId,
             String roomDisplayNameSnapshot,
             Instant startTime,
             Instant endTime,
             int capacity,
             WorkshopState state,
             Instant createdAt,
             Instant updatedAt) {
        this.id = id;
        this.title = validateTitle(title);
        this.description = description;
        this.roomId = roomId;
        this.roomDisplayNameSnapshot = roomDisplayNameSnapshot;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.state = state;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ============ Factory Methods (Aggregate Behaviors) ============

    static Workshop createDraft(String title, String description) {
        return new Workshop(title, description);
    }

    /**
     * Schedules the workshop (stores scheduling info without publishing).
     * Only allowed in DRAFT state.
     * Does NOT validate room conflict - that happens at publish().
     */
    void schedule(UUID roomId, String roomDisplayNameSnapshot, Instant startTime, Instant endTime, int capacity) {
        if (state != WorkshopState.DRAFT) {
            throw new InvalidWorkshopStateException("Can only schedule workshop in DRAFT state, current: " + state);
        }
        if (roomId == null) {
            throw new IllegalArgumentException("roomId must not be null");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("startTime must not be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("endTime must not be null");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than 0");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }

        this.roomId = roomId;
        this.roomDisplayNameSnapshot = roomDisplayNameSnapshot;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.updatedAt = Instant.now();
    }

    // ============ State Transition Methods ============

    /**
     * Publishes the workshop.
     * Validates all publishing invariants.
     * Transitions: DRAFT -> PUBLISHED
     */
    void publish(UUID roomId, String roomDisplayNameSnapshot, Instant startTime, Instant endTime, int capacity) {
        validateStateTransition(WorkshopState.PUBLISHED);
        validatePublishingInvariants(roomId, startTime, endTime, capacity);

        this.roomId = roomId;
        this.roomDisplayNameSnapshot = roomDisplayNameSnapshot;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.state = WorkshopState.PUBLISHED;
        this.updatedAt = Instant.now();
    }

    /**
     * Reschedules the workshop.
     * Transitions: PUBLISHED -> PUBLISHED (stays PUBLISHED)
     */
    void reschedule(Instant startTime, Instant endTime, UUID newRoomId, String newRoomDisplayNameSnapshot, boolean roomChanged) {
        if (state != WorkshopState.PUBLISHED) {
            throw new InvalidWorkshopStateException("Cannot reschedule workshop in state: " + state);
        }
        validateRescheduleInvariants(startTime, endTime);

        this.startTime = startTime;
        this.endTime = endTime;
        if (roomChanged) {
            this.roomId = newRoomId;
            this.roomDisplayNameSnapshot = newRoomDisplayNameSnapshot;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Starts the workshop.
     * Transitions: PUBLISHED -> IN_PROGRESS
     */
    void start() {
        validateStateTransition(WorkshopState.IN_PROGRESS);
        this.state = WorkshopState.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    /**
     * Completes the workshop.
     * Transitions: IN_PROGRESS -> COMPLETED
     */
    void complete() {
        validateStateTransition(WorkshopState.COMPLETED);
        this.state = WorkshopState.COMPLETED;
        this.updatedAt = Instant.now();
    }

    /**
     * Cancels the workshop.
     * Transitions: DRAFT -> CANCELLED, PUBLISHED -> CANCELLED, IN_PROGRESS -> CANCELLED
     */
    void cancel() {
        if (state == WorkshopState.COMPLETED || state == WorkshopState.CANCELLED) {
            throw new InvalidWorkshopStateException("Cannot cancel workshop in state: " + state);
        }
        this.state = WorkshopState.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates content (title, description).
     * Allowed in: DRAFT, PUBLISHED
     */
    void updateContent(String title, String description) {
        if (state == WorkshopState.IN_PROGRESS || state == WorkshopState.COMPLETED || state == WorkshopState.CANCELLED) {
            throw new InvalidWorkshopStateException("Cannot update content in state: " + state);
        }
        this.title = validateTitle(title);
        this.description = description;
        this.updatedAt = Instant.now();
    }

    // ============ Validation Methods ============

    private void validateStateTransition(WorkshopState targetState) {
        if (!canTransitionTo(targetState)) {
            throw new InvalidWorkshopStateException(
                "Invalid state transition from %s to %s".formatted(this.state, targetState)
            );
        }
    }

    private boolean canTransitionTo(WorkshopState targetState) {
        return switch (this.state) {
            case DRAFT -> targetState == WorkshopState.PUBLISHED || targetState == WorkshopState.CANCELLED;
            case PUBLISHED -> targetState == WorkshopState.IN_PROGRESS || targetState == WorkshopState.CANCELLED;
            case IN_PROGRESS -> targetState == WorkshopState.COMPLETED || targetState == WorkshopState.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private void validatePublishingInvariants(UUID roomId, Instant startTime, Instant endTime, int capacity) {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId must not be null");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("startTime must not be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("endTime must not be null");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than 0");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
        if (!startTime.isAfter(Instant.now())) {
            throw new IllegalArgumentException("startTime must be in the future");
        }
    }

    private void validateRescheduleInvariants(Instant startTime, Instant endTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("startTime must not be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("endTime must not be null");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
    }

    private String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("title must not exceed 200 characters");
        }
        return title;
    }

    // ============ Getters ============

    UUID getId() { return id; }
    String getTitle() { return title; }
    String getDescription() { return description; }
    UUID getRoomId() { return roomId; }
    String getRoomDisplayNameSnapshot() { return roomDisplayNameSnapshot; }
    Instant getStartTime() { return startTime; }
    Instant getEndTime() { return endTime; }
    int getCapacity() { return capacity; }
    WorkshopState getState() { return state; }
    Instant getCreatedAt() { return createdAt; }
    Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Workshop workshop = (Workshop) o;
        return java.util.Objects.equals(id, workshop.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}