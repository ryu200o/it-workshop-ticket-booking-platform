package com.example.itworkshopticketbookingplatform.workshop.internal.domain.event;

import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopId;
import java.time.Instant;
import java.util.UUID;

public record WorkshopRoomChangedEvent(
    WorkshopId workshopId,
    UUID oldRoomId,
    UUID newRoomId,
    String newRoomDisplayName,
    Instant changedAt
) {}