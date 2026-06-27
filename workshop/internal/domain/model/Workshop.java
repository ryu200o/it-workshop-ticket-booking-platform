package com.example.itworkshopticketbookingplatform.workshop.internal.domain.model;

import java.time.Instant;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Aggregate Root representing a Workshop.
 * Follows domain-driven design principles with business rules and invariants.
 */
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

    // Domain invariants and validation methods
    private void requireValidCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
    }

    private void requireValidTimestamps(Instant startTime, Instant endTime) {
        Objects.requireNonNull(startTime, "Start time cannot be null");
        Objects.requireNonNull(endTime, "End time cannot be null");
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private void requireFutureOrCurrent(Instant time) {
        Instant now = Instant.now();
        if (time.isBefore(now)) {
            throw new IllegalArgumentException("Start time must be in the future");
        }
    }

    // State transition guards
    private boolean canUpdateFromDraft() {
        return state == WorkshopState.DRAFT;
    }

    private boolean canScheduleFromDraft() {
        return state == WorkshopState.DRAFT;
    }

    private boolean canPublishFromDraft() {
        return state == WorkshopState.DRAFT && isDraftComplete();
    }

    private boolean canRescheduleFromPublished() {
        return state == WorkshopState.PUBLISHED;
    }

    private boolean canStartFromPublished() {
        return state == WorkshopState.PUBLISHED;
    }

    private boolean canCompleteFromInProgress() {
        return state == WorkshopState.IN_PROGRESS;
    }

    private boolean canCancelFromAny() {
        return state == WorkshopState.PUBLISHED ||
               state == WorkshopState.IN_PROGRESS ||
               state == WorkshopState.COMPLETED;
    }

    // State completeness validation
    private boolean isDraftComplete() {
        return roomId != null &&
               startTime != null &&
               endTime != null &&
               capacity > 0 &&
               startTime.isAfter(Instant.now());
    }

    // Aggregate behaviors

    /**
     * Static factory to create a new workshop in DRAFT state.
     */
    public static Workshop createDraft(@NonNull String title, @NonNull String description) {
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        
        Workshop workshop = new Workshop();
        workshop.id = WorkshopId.create();
        workshop.title = title;
        workshop.description = description;
        workshop.state = WorkshopState.DRAFT;
        workshop.createdAt = Instant.now();
        workshop.updatedAt = Instant.now();
        
        return workshop;
    }

    /**
     * Updates workshop content (title and description) if in DRAFT state.
     */
    public void updateContent(String title, String description) {
        requireValidUpdateContentState();
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        
        this.title = title;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    /**
     * Schedules the workshop (sets room, times, and capacity) if in DRAFT state.
     */
    public void schedule(@NonNull UUID roomId, @NonNull String roomDisplayNameSnapshot,
                        @NonNull Instant startTime, @NonNull Instant endTime, int capacity) {
        requireValidScheduleState(roomId, roomDisplayNameSnapshot, startTime, endTime, capacity);
        
        this.roomId = roomId;
        this.roomDisplayNameSnapshot = roomDisplayNameSnapshot;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.updatedAt = Instant.now();
    }

    /**
     * Publishes the workshop if all publishing invariants are satisfied.
     */
    public void publish() {
        requireValidPublishState();
        this.state = WorkshopState.PUBLISHED;
        this.updatedAt = Instant.now();
    }

    /**
     * Reschedules the workshop (changes room, times, and capacity) if in PUBLISHED state.
     */
    public void reschedule(@NonNull UUID roomId, @NonNull String roomDisplayNameSnapshot,
                          @NonNull Instant startTime, @NonNull Instant endTime, int capacity) {
        requireValidRescheduleState(roomId, startTime, endTime);
        
        // Lock capacity field (cannot be changed)
        this.roomId = roomId;
        this.roomDisplayNameSnapshot = roomDisplayNameSnapshot;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.updatedAt = Instant.now();
    }

    /**
     * Starts the workshop if in PUBLISHED state.
     */
    public void start() {
        requireValidStartState();
        this.state = WorkshopState.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    /**
     * Completes the workshop if in IN_PROGRESS state.
     */
    public void complete() {
        requireValidCompleteState();
        this.state = WorkshopState.COMPLETED;
        this.updatedAt = Instant.now();
    }

    /**
     * Cancels the workshop if in PUBLISHED, IN_PROGRESS, or COMPLETED state.
     */
    public void cancel() {
        requireValidCancelState();
        this.state = WorkshopState.CANCELLED;
        this.updatedAt = Instant.now();
    }

    // State transition validation helpers
    private void requireValidUpdateContentState() {
        if (!canUpdateFromDraft()) {
            throw new InvalidWorkshopStateException(
                String.format("Cannot update content from state %s", state));
        }
    }

    private void requireValidScheduleState(@NonNull UUID roomId, @NonNull String roomDisplayNameSnapshot,
                                         @NonNull Instant startTime, @NonNull Instant endTime, int capacity) {
        if (!canScheduleFromDraft()) {
            throw new InvalidWorkshopStateException(
                String.format("Cannot schedule from state %s", state));
        }
        requireValidTimestamps(startTime, endTime);
        requireFutureOrCurrent(startTime);
        requireValidCapacity(capacity);
        Objects.requireNonNull(roomId, "Room ID cannot be null");
        Objects.requireNonNull(roomDisplayNameSnapshot, "Room display name snapshot cannot be null");
    }

    private void requireValidPublishState() {
        if (!canPublishFromDraft()) {
            throw new InvalidWorkshopStateException(
                String.format("Cannot publish from incomplete draft state %s", state));
        }
    }

    private void requireValidRescheduleState(@NonNull UUID roomId, @NonNull Instant startTime, @NonNull Instant endTime) {
        if (!canRescheduleFromPublished()) {
            throw new InvalidWorkshopStateException(
                String.format("Cannot reschedule from state %s", state));
        }
        requireValidTimestamps(startTime, endTime);
        requireFutureOrCurrent(startTime);
        Objects.requireNonNull(roomId, "Room ID cannot be null");
    }

    private void requireValidStartState() {
        if (!canStartFromPublished()) {
            throw new InvalidWorkshopStateException(
                String.format("Cannot start from state %s", state));
        }
    }

    private void requireValidCompleteState() {
        if (!canCompleteFromInProgress()) {
            throw new InvalidWorkshopStateException(
                String.format("Cannot complete from state %s", state));
        }
    }

    private void requireValidCancelState() {
        if (!canCancelFromAny()) {
            throw new InvalidWorkshopStateException(
                String.format("Cannot cancel from state %s", state));
        }
    }

    // Getters

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

    // Equality and hash

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Workshop that = (Workshop) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Workshop{" +
               "id=" + id +
               ", title='" + title + "'" +
               ", state=" + state +
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", capacity=" + capacity +
               '}';
    }

    // Private default constructor for factory method
    private Workshop() {
        this.createdAt = Instant.now();
    }
}