package com.example.itworkshopticketbookingplatform.registration.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegistrationRequest(
    @NotNull UUID workshopId,
    @NotNull UUID userId
) {}
