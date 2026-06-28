package com.example.itworkshopticketbookingplatform.room.internal;

import java.util.UUID;

class RoomNotFoundException extends RuntimeException {
    RoomNotFoundException(UUID id) {
        super("Room not found with ID: " + id);
    }

    RoomNotFoundException(String roomCode) {
        super("Room not found with room code: " + roomCode);
    }
}