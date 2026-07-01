package com.example.itworkshopticketbookingplatform.registration.dto;

import java.time.Instant;
import java.util.UUID;

public record RegistrationResponse(
    UUID id,
    UUID workshopId,
    UUID userId,
    String status,
    Instant registrationTime,
    boolean checkedIn,
    Instant checkedInAt,
    UUID checkedInBy,
    Instant createdAt,
    Instant updatedAt
) {}
