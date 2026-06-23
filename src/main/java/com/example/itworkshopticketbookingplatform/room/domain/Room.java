package com.example.itworkshopticketbookingplatform.room.domain;

import jakarta.persistence.*;
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Room() {
    }

    public Room(UUID id, String roomCode, int physicalCapacity, String location, boolean active) {
        this.id = Objects.requireNonNull(id, "Room ID cannot be null");
        setRoomCode(roomCode);
        setPhysicalCapacity(physicalCapacity);
        setLocation(location);
        this.active = active;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            throw new IllegalArgumentException("Room code cannot be null or blank");
        }
        this.roomCode = roomCode;
        this.updatedAt = LocalDateTime.now();
    }

    public int getPhysicalCapacity() {
        return physicalCapacity;
    }

    public void setPhysicalCapacity(int physicalCapacity) {
        if (physicalCapacity <= 0) {
            throw new IllegalArgumentException("Physical capacity must be greater than 0");
        }
        this.physicalCapacity = physicalCapacity;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location cannot be null or blank");
        }
        this.location = location;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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
               ", location='" + location + '\'' +
               ", active=" + active +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}
