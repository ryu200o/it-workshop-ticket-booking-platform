package com.example.itworkshopticketbookingplatform.room.internal;

import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.example.itworkshopticketbookingplatform.room.internal.exception.InvalidRoomCodeException;
import com.example.itworkshopticketbookingplatform.room.internal.exception.InvalidPhysicalCapacityException;
import com.example.itworkshopticketbookingplatform.room.internal.exception.InvalidLocationException;

@Entity
@Table(name = "rooms")
class Room {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", unique = true, nullable = false)
    private String roomCode;

    @Column(name = "capacity", nullable = false)
    private int physicalCapacity;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // No-args constructor for JPA
    protected Room() {}

    // Creation Constructor (automatically sets active=true, createdAt and updatedAt)
    Room(@NonNull UUID id, @NonNull String roomCode, int physicalCapacity, @NonNull String location) {
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
    Room(@NonNull UUID id, @NonNull String roomCode, int physicalCapacity, @NonNull String location, boolean active, @NonNull LocalDateTime createdAt, @NonNull LocalDateTime updatedAt) {
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

    void rename(@NonNull String newRoomCode) {
        requireValidRoomCode(newRoomCode);
        this.roomCode = newRoomCode;
        this.updatedAt = LocalDateTime.now();
    }

    void changeCapacity(int newCapacity) {
        requirePositiveCapacity(newCapacity);
        this.physicalCapacity = newCapacity;
        this.updatedAt = LocalDateTime.now();
    }

    void changeLocation(@NonNull String newLocation) {
        requireValidLocation(newLocation);
        this.location = newLocation;
        this.updatedAt = LocalDateTime.now();
    }

    void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    UUID getId() { return id; }
    String getRoomCode() { return roomCode; }
    int getPhysicalCapacity() { return physicalCapacity; }
    String getLocation() { return location; }
    boolean isActive() { return active; }
    LocalDateTime getCreatedAt() { return createdAt; }
    LocalDateTime getUpdatedAt() { return updatedAt; }

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