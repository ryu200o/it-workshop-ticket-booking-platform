package com.example.itworkshopticketbookingplatform.room.internal.domain.model;

import com.example.itworkshopticketbookingplatform.room.internal.domain.exception.InvalidRoomCodeException;
import com.example.itworkshopticketbookingplatform.room.internal.domain.exception.InvalidPhysicalCapacityException;
import com.example.itworkshopticketbookingplatform.room.internal.domain.exception.InvalidLocationException;

import org.jspecify.annotations.NonNull;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Room {

    private final UUID id;
    private String roomCode;
    private int physicalCapacity;
    private String location;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Creation Constructor (automatically sets active=true, createdAt and updatedAt)
    public Room(@NonNull UUID id, @NonNull String roomCode, int physicalCapacity, @NonNull String location) {
        this.id = Objects.requireNonNull(id, "Room ID cannot be null");
        requireValidRoomCode(roomCode);
        requirePositiveCapacity(physicalCapacity);
        requireValidLocation(location);
        
        this.roomCode = roomCode;
        this.physicalCapacity = physicalCapacity;
        this.location = location;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Persistence Reconstruction Constructor (sets historical timestamps)
    public Room(@NonNull UUID id, @NonNull String roomCode, int physicalCapacity, @NonNull String location, boolean active, @NonNull LocalDateTime createdAt, @NonNull LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Room ID cannot be null");
        this.roomCode = Objects.requireNonNull(roomCode, "Room code cannot be null");
        this.physicalCapacity = physicalCapacity;
        this.location = Objects.requireNonNull(location, "Location cannot be null");
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
    }

    private void requireValidRoomCode(@NonNull String code) {
        if (code.isBlank()) {
            throw new InvalidRoomCodeException("Room code cannot be blank");
        }
    }

    private void requirePositiveCapacity(int capacity) {
        if (capacity <= 0) {
            throw new InvalidPhysicalCapacityException("Physical capacity must be greater than 0");
        }
    }

    private void requireValidLocation(@NonNull String loc) {
        if (loc.isBlank()) {
            throw new InvalidLocationException("Location cannot be blank");
        }
    }

    public void rename(@NonNull String newRoomCode) {
        requireValidRoomCode(newRoomCode);
        this.roomCode = newRoomCode;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeCapacity(int newCapacity) {
        requirePositiveCapacity(newCapacity);
        this.physicalCapacity = newCapacity;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeLocation(@NonNull String newLocation) {
        requireValidLocation(newLocation);
        this.location = newLocation;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public UUID getId() { return id; }
    public String getRoomCode() { return roomCode; }
    public int getPhysicalCapacity() { return physicalCapacity; }
    public String getLocation() { return location; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Room{" +
               "id=" + id +
               ", roomCode='" + roomCode + '\'' +
               ", physicalCapacity=" + physicalCapacity +
               ", active=" + active +
               '}';
    }
}
