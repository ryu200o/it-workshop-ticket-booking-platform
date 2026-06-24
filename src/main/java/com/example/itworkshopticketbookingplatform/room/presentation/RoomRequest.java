package com.example.itworkshopticketbookingplatform.room.presentation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
public record RoomRequest(
    @NotBlank(message = "Room code cannot be blank") String roomCode,
    @Positive(message = "Physical capacity must be greater than 0") int physicalCapacity,
    @NotBlank(message = "Location cannot be blank") String location
) {}
