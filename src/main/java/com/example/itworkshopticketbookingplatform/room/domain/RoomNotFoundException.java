package com.example.itworkshopticketbookingplatform.room.domain;

import java.util.UUID;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(UUID id) {
        super("Room not found with ID: " + id);
    }

    public RoomNotFoundException(String roomCode) {
        super("Room not found with room code: " + roomCode);
    }
}