package com.example.itworkshopticketbookingplatform.workshop.internal.domain.model;

import com.example.itworkshopticketbookingplatform.workshop.internal.domain.exception.InvalidWorkshopStateException;

import java.time.Instant;
import java.util.UUID;

public class Workshop {

    private final WorkshopId id;
    private String title;
    private String description;
    private UUID roomId;
    private String roomDisplayNameSnapshot;
    private Instant startTime;
    private Instant endTime;
    private int capacity;
    private WorkshopState state;
    private final Instant createdAt;
    private Instant updatedAt;

    // ============ Constructors ============

    /**
     * Creates a new Workshop in DRAFT state.
     * Used by createDraft() aggregate behavior.
     */
    public Workshop(String title, String description) {
        this.id = WorkshopId.generate();
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
    Workshop(WorkshopId id,
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

    public static Workshop createDraft(String title, String description) {
        return new Workshop(title, description);
    }

    /**
     * Schedules the workshop (stores scheduling info without publishing).
     * Only allowed in DRAFT state.
     * Does NOT validate room conflict - that happens at publish().
     */
    public void schedule(UUID roomId, String roomDisplayNameSnapshot, Instant startTime, Instant endTime, int capacity) {
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
    public void publish(UUID roomId, String roomDisplayNameSnapshot, Instant startTime, Instant endTime, int capacity) {
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
    public void reschedule(Instant startTime, Instant endTime, UUID newRoomId, String newRoomDisplayNameSnapshot, boolean roomChanged) {
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
    public void start() {
        validateStateTransition(WorkshopState.IN_PROGRESS);
        this.state = WorkshopState.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    /**
     * Completes the workshop.
     * Transitions: IN_PROGRESS -> COMPLETED
     */
    public void complete() {
        validateStateTransition(WorkshopState.COMPLETED);
        this.state = WorkshopState.COMPLETED;
        this.updatedAt = Instant.now();
    }

    /**
     * Cancels the workshop.
     * Transitions: DRAFT -> CANCELLED, PUBLISHED -> CANCELLED, IN_PROGRESS -> CANCELLED
     */
    public void cancel() {
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
    public void updateContent(String title, String description) {
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

    public WorkshopId getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public UUID getRoomId() { return roomId; }
    public String getRoomDisplayNameSnapshot() { return roomDisplayNameSnapshot; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public int getCapacity() { return capacity; }
    public WorkshopState getState() { return state; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ============ Persistence Factory ============

    /**
     * Creates Workshop instance from persistence data.
     * Package-private - only infrastructure layer should use this.
     */
    public static Workshop fromPersistence(WorkshopId id,
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
        return new Workshop(id, title, description, roomId, roomDisplayNameSnapshot,
                startTime, endTime, capacity, state, createdAt, updatedAt);
    }
}