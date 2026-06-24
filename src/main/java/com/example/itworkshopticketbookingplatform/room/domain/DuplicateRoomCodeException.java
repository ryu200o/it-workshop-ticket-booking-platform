package com.example.itworkshopticketbookingplatform.room.domain;

public class DuplicateRoomCodeException extends RuntimeException {
    public DuplicateRoomCodeException(String roomCode) {
        super("Room with code '" + roomCode + "' already exists.");
    }
}