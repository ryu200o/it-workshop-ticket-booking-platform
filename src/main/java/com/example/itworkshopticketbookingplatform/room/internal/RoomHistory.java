package com.example.itworkshopticketbookingplatform.room.internal;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "room_histories")
class RoomHistory {

    @Id
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "changed_by", nullable = false)
    private UUID changedBy;

    @Column(name = "reason")
    private String reason;

    @Column(name = "changes", columnDefinition = "JSONB", nullable = false)
    private Map<String, Object> changes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RoomHistory() {}

    RoomHistory(UUID id, UUID roomId, Instant changedAt, UUID changedBy, String reason, Map<String, Object> changes) {
        this.id = id;
        this.roomId = roomId;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
        this.reason = reason;
        this.changes = changes;
        this.createdAt = Instant.now();
    }

    // Getters
    UUID getId() { return id; }
    UUID getRoomId() { return roomId; }
    Instant getChangedAt() { return changedAt; }
    UUID getChangedBy() { return changedBy; }
    String getReason() { return reason; }
    Map<String, Object> getChanges() { return changes; }
    Instant getCreatedAt() { return createdAt; }
}
