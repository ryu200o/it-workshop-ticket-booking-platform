package com.example.itworkshopticketbookingplatform.room.internal;

class DuplicateRoomCodeException extends RuntimeException {
    DuplicateRoomCodeException(String roomCode) {
        super("Room with code '" + roomCode + "' already exists.");
    }
}