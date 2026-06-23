package com.example.itworkshopticketbookingplatform.room.presentation;

import com.example.itworkshopticketbookingplatform.room.domain.Room;
import java.time.LocalDateTime;
import java.util.UUID;

public class RoomResponse {
    private UUID id;
    private String roomCode;
    private int physicalCapacity;
    private String location;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RoomResponse(Room room) {
        this.id = room.getId();
        this.roomCode = room.getRoomCode();
        this.physicalCapacity = room.getPhysicalCapacity();
        this.location = room.getLocation();
        this.active = room.isActive();
        this.createdAt = room.getCreatedAt();
        this.updatedAt = room.getUpdatedAt();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public int getPhysicalCapacity() {
        return physicalCapacity;
    }

    public String getLocation() {
        return location;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
