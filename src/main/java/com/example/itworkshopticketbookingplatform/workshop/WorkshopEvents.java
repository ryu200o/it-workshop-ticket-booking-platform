package com.example.itworkshopticketbookingplatform.workshop;

import java.time.Instant;
import java.util.UUID;

public final class WorkshopEvents {
    private WorkshopEvents() {} // Prevent instantiation

    // Root interface enforcing core attributes across all workshop events
    public sealed interface WorkshopEvent permits Published, Started, Completed, Cancelled, Rescheduled, RoomChanged {
        UUID workshopId();
        Instant occurredAt();
    }

    public record Published(UUID workshopId, String title, Instant startTime, Instant endTime,
                            int capacity, UUID roomId, String roomDisplayNameSnapshot,
                            Instant occurredAt) implements WorkshopEvent {}

    public record Started(UUID workshopId, Instant occurredAt) implements WorkshopEvent {}
    public record Completed(UUID workshopId, Instant occurredAt) implements WorkshopEvent {}
    public record Cancelled(UUID workshopId, String reason, Instant occurredAt) implements WorkshopEvent {}

    public record Rescheduled(UUID workshopId, Instant oldStartTime, Instant newStartTime,
                              Instant oldEndTime, Instant newEndTime, boolean roomIdChanged,
                              String newRoomDisplayName, Instant occurredAt) implements WorkshopEvent {}

    public record RoomChanged(UUID workshopId, UUID oldRoomId, UUID newRoomId,
                              String newRoomDisplayName, Instant occurredAt) implements WorkshopEvent {}
}