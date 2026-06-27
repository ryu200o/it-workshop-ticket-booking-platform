package com.example.itworkshopticketbookingplatform.workshop.internal.domain.model;

import java.util.UUID;

public record WorkshopId(UUID value) {
    public static WorkshopId generate() { return new WorkshopId(UUID.randomUUID()); }
    public static WorkshopId of(String value) { return new WorkshopId(UUID.fromString(value)); }
}