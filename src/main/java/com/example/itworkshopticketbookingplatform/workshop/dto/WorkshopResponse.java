package com.example.itworkshopticketbookingplatform.workshop.dto;

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
    String state,
    Instant createdAt,
    Instant updatedAt
) {}