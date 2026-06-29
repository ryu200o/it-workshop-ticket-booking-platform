package com.example.itworkshopticketbookingplatform.room.internal.exception;

public class DuplicateRoomCodeException extends RuntimeException {
    public DuplicateRoomCodeException(String roomCode) {
        super("Room with code '" + roomCode + "' already exists.");
    }
}