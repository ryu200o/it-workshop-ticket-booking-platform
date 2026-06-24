package com.example.itworkshopticketbookingplatform.room.presentation;
import com.example.itworkshopticketbookingplatform.room.domain.Room;
import java.time.LocalDateTime;
import java.util.UUID;
public record RoomResponse(
    UUID id, String roomCode, int physicalCapacity, String location, 
    boolean active, LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public RoomResponse(Room room) {
        this(room.getId(), room.getRoomCode(), room.getPhysicalCapacity(), room.getLocation(), 
             room.isActive(), room.getCreatedAt(), room.getUpdatedAt());
    }
}
