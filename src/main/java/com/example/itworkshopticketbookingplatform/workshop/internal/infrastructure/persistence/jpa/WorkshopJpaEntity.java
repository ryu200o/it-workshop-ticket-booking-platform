package com.example.itworkshopticketbookingplatform.workshop.internal.infrastructure.persistence.jpa;

import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopState;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workshop")
public class WorkshopJpaEntity {

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
    protected WorkshopJpaEntity() {}

    public WorkshopJpaEntity(UUID id, String title, String description, UUID roomId,
                            String roomDisplayNameSnapshot, Instant startTime, Instant endTime,
                            int capacity, WorkshopState state, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
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

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

    public String getRoomDisplayNameSnapshot() { return roomDisplayNameSnapshot; }
    public void setRoomDisplayNameSnapshot(String roomDisplayNameSnapshot) { this.roomDisplayNameSnapshot = roomDisplayNameSnapshot; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public WorkshopState getState() { return state; }
    public void setState(WorkshopState state) { this.state = state; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}