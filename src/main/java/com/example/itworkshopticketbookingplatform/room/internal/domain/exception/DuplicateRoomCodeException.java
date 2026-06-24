package com.example.itworkshopticketbookingplatform.room.internal.domain.exception;

public class DuplicateRoomCodeException extends RuntimeException {
    public DuplicateRoomCodeException(String roomCode) {
        super("Room with code '" + roomCode + "' already exists.");
    }
}
