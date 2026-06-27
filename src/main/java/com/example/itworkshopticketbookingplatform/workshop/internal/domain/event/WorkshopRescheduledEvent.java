package com.example.itworkshopticketbookingplatform.workshop.internal.domain.event;

import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopId;
import java.time.Instant;
import java.util.UUID;

public record WorkshopRescheduledEvent(
    WorkshopId workshopId,
    Instant oldStartTime,
    Instant newStartTime,
    Instant oldEndTime,
    Instant newEndTime,
    boolean roomIdChanged,
    String newRoomDisplayName,
    Instant rescheduledAt
) {}