package com.example.itworkshopticketbookingplatform.room.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class RoomRequest {
    @NotBlank(message = "Room code cannot be blank")
    private String roomCode;
    @Positive(message = "Physical capacity must be greater than 0")
    private int physicalCapacity;
    @NotBlank(message = "Location cannot be blank")
    private String location;

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public int getPhysicalCapacity() {
        return physicalCapacity;
    }

    public void setPhysicalCapacity(int physicalCapacity) {
        this.physicalCapacity = physicalCapacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
