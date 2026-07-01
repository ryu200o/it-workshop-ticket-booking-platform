package com.example.itworkshopticketbookingplatform.registration;

import java.time.Instant;
import java.util.UUID;

public final class RegistrationEvents {
    private RegistrationEvents() {}

    public sealed interface RegistrationEvent permits Registered, Cancelled, Attended, NoShow {
        UUID registrationId();
        UUID workshopId();
        Instant occurredAt();
    }

    public record Registered(UUID registrationId, UUID workshopId, UUID userId, Instant occurredAt) implements RegistrationEvent {}
    public record Cancelled(UUID registrationId, UUID workshopId, UUID userId, String reason, Instant occurredAt) implements RegistrationEvent {}
    public record Attended(UUID registrationId, UUID workshopId, UUID userId, UUID checkedInBy, Instant occurredAt) implements RegistrationEvent {}
    public record NoShow(UUID registrationId, UUID workshopId, UUID userId, Instant occurredAt) implements RegistrationEvent {}
}
