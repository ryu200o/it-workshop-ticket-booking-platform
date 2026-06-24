package com.example.itworkshopticketbookingplatform.room.domain;

import jakarta.persistence.*;
import org.springframework.lang.NonNull;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
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

    protected Room() {
    }

    public Room(@NonNull UUID id, @NonNull String roomCode, int physicalCapacity, @NonNull String location) {
        this.id = Objects.requireNonNull(id, "Room ID cannot be null");
        requireValidRoomCode(roomCode);
        requirePositiveCapacity(physicalCapacity);
        requireValidLocation(location);
        
        this.roomCode = roomCode;
        this.physicalCapacity = physicalCapacity;
        this.location = location;
        this.active = true;
    }

    private void requireValidRoomCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidRoomCodeException("Room code cannot be null or blank");
        }
    }

    private void requirePositiveCapacity(int capacity) {
        if (capacity <= 0) {
            throw new InvalidPhysicalCapacityException("Physical capacity must be greater than 0");
        }
    }

    private void requireValidLocation(String loc) {
        if (loc == null || loc.isBlank()) {
            throw new InvalidLocationException("Location cannot be null or blank");
        }
    }

    public void rename(@NonNull String newRoomCode) {
        requireValidRoomCode(newRoomCode);
        this.roomCode = newRoomCode;
    }

    public void changeCapacity(int newCapacity) {
        requirePositiveCapacity(newCapacity);
        this.physicalCapacity = newCapacity;
    }

    public void changeLocation(@NonNull String newLocation) {
        requireValidLocation(newLocation);
        this.location = newLocation;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
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
