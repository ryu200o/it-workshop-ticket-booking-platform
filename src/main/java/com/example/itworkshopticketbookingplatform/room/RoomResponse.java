package com.example.itworkshopticketbookingplatform.room;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoomResponse(
    UUID id, String roomCode, int physicalCapacity, String location, 
    boolean active, LocalDateTime createdAt, LocalDateTime updatedAt
) {}
