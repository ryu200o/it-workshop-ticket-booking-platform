package com.example.itworkshopticketbookingplatform.workshop.internal.domain.event;

import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopId;
import java.time.Instant;
import java.util.UUID;

public record WorkshopPublishedEvent(
    WorkshopId workshopId,
    String title,
    Instant startTime,
    Instant endTime,
    int capacity,
    UUID roomId,
    String roomDisplayNameSnapshot,
    Instant publishedAt
) {}