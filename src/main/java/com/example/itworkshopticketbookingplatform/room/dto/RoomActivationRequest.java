package com.example.itworkshopticketbookingplatform.room.dto;

import jakarta.validation.constraints.NotNull;

public record RoomActivationRequest(
    @NotNull(message = "Active status cannot be null") Boolean active
) {}
