package com.example.itworkshopticketbookingplatform.workshop;

import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopState;
import java.time.Instant;
import java.util.UUID;

public record WorkshopResponse(
    UUID id,
    String title,
    String description,
    UUID roomId,
    String roomDisplayNameSnapshot,
    Instant startTime,
    Instant endTime,
    int capacity,
    WorkshopState state,
    Instant createdAt,
    Instant updatedAt
) {}