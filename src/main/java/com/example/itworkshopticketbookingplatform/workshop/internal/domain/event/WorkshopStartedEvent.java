package com.example.itworkshopticketbookingplatform.workshop.internal.domain.event;

import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopId;
import java.time.Instant;

public record WorkshopStartedEvent(
    WorkshopId workshopId,
    Instant startedAt
) {}