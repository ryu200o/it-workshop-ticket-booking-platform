package com.example.itworkshopticketbookingplatform.room.domain;
public class InvalidRoomCodeException extends RoomDomainException {
    public InvalidRoomCodeException(String message) { super(message); }
}