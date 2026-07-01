package com.example.itworkshopticketbookingplatform.workshop.internal;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workshop_snapshots")
class WorkshopSnapshot {

    @Id
    private UUID id;

    @Column(name = "workshop_id", nullable = false)
    private UUID workshopId;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(name = "room_location", nullable = false)
    private String roomLocation;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "actual_attendance", nullable = false)
    private int actualAttendance;

    @Column(name = "feedback_score", precision = 3, scale = 2)
    private BigDecimal feedbackScore;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected WorkshopSnapshot() {}

    WorkshopSnapshot(UUID id, UUID workshopId, String roomName, String roomLocation,
                     Instant startTime, Instant endTime, int capacity,
                     int actualAttendance, Instant completedAt) {
        this.id = id;
        this.workshopId = workshopId;
        this.roomName = roomName;
        this.roomLocation = roomLocation;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.actualAttendance = actualAttendance;
        this.feedbackScore = null;
        this.completedAt = completedAt;
        this.createdAt = Instant.now();
    }

    // Getters
    UUID getId() { return id; }
    UUID getWorkshopId() { return workshopId; }
    String getRoomName() { return roomName; }
    String getRoomLocation() { return roomLocation; }
    Instant getStartTime() { return startTime; }
    Instant getEndTime() { return endTime; }
    int getCapacity() { return capacity; }
    int getActualAttendance() { return actualAttendance; }
    BigDecimal getFeedbackScore() { return feedbackScore; }
    Instant getCompletedAt() { return completedAt; }
    Instant getCreatedAt() { return createdAt; }
}
