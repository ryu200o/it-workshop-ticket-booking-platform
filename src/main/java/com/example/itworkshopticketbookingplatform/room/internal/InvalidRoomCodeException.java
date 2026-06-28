package com.example.itworkshopticketbookingplatform.room.internal;

class InvalidRoomCodeException extends RoomDomainException {
    InvalidRoomCodeException(String message) { super(message); }
}