package com.example.itworkshopticketbookingplatform.workshop.internal;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "workshop_histories")
class WorkshopHistory {

    @Id
    private UUID id;

    @Column(name = "workshop_id", nullable = false)
    private UUID workshopId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "event_data", columnDefinition = "JSONB", nullable = false)
    private Map<String, Object> eventData;

    @Column(name = "reason")
    private String reason;

    @Column(name = "changed_by", nullable = false)
    private UUID changedBy;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected WorkshopHistory() {}

    WorkshopHistory(UUID id, UUID workshopId, String eventType, Map<String, Object> eventData,
                    String reason, UUID changedBy, Instant occurredAt) {
        this.id = id;
        this.workshopId = workshopId;
        this.eventType = eventType;
        this.eventData = eventData;
        this.reason = reason;
        this.changedBy = changedBy;
        this.occurredAt = occurredAt;
        this.createdAt = Instant.now();
    }

    // Getters
    UUID getId() { return id; }
    UUID getWorkshopId() { return workshopId; }
    String getEventType() { return eventType; }
    Map<String, Object> getEventData() { return eventData; }
    String getReason() { return reason; }
    UUID getChangedBy() { return changedBy; }
    Instant getOccurredAt() { return occurredAt; }
    Instant getCreatedAt() { return createdAt; }
}
