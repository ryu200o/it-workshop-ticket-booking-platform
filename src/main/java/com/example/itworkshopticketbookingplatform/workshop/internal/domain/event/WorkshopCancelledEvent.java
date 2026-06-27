package com.example.itworkshopticketbookingplatform.workshop.internal.domain.event;

import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopId;
import java.time.Instant;

public record WorkshopCancelledEvent(
    WorkshopId workshopId,
    Instant cancelledAt,
    String reason
) {}