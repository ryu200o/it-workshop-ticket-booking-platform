package com.example.itworkshopticketbookingplatform.room.internal;

final class RoomExceptions {
    private RoomExceptions() {} // Prevent instantiation

    static class RoomDomainException extends RuntimeException {
        RoomDomainException(String message) { super(message); }
    }

    static class InvalidRoomCodeException extends RoomDomainException {
        InvalidRoomCodeException(String message) { super(message); }
    }

    static class InvalidPhysicalCapacityException extends RoomDomainException {
        InvalidPhysicalCapacityException(String message) { super(message); }
    }

    static class InvalidLocationException extends RoomDomainException {
        InvalidLocationException(String message) { super(message); }
    }

    static class DuplicateRoomCodeException extends RuntimeException {
        DuplicateRoomCodeException(String roomCode) {
            super("Room with code '" + roomCode + "' already exists.");
        }
    }
}
