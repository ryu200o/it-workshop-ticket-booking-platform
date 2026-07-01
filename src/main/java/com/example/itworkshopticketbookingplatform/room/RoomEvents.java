package com.example.itworkshopticketbookingplatform.room;

import java.time.Instant;
import java.util.UUID;

public final class RoomEvents {
    private RoomEvents() {}

    public sealed interface RoomEvent permits RoomRenamed, RoomLocationChanged, RoomDeactivated {
        UUID roomId();
        Instant occurredAt();
    }

    public record RoomRenamed(UUID roomId, String oldName, String newName, Instant occurredAt) implements RoomEvent {}
    public record RoomLocationChanged(UUID roomId, String oldLocation, String newLocation, Instant occurredAt) implements RoomEvent {}
    public record RoomDeactivated(UUID roomId, boolean wasActive, Instant occurredAt) implements RoomEvent {}
}
