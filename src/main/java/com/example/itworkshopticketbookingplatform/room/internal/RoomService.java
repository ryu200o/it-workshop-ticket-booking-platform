package com.example.itworkshopticketbookingplatform.room.internal;

import org.jspecify.annotations.NonNull;
import java.util.List;
import java.util.UUID;

import com.example.itworkshopticketbookingplatform.room.dto.RoomResponse;

interface RoomService {
    RoomResponse createRoom(@NonNull String roomCode, int physicalCapacity, @NonNull String location);
    RoomResponse updateRoom(@NonNull UUID id, @NonNull String roomCode, int physicalCapacity, @NonNull String location);
    RoomResponse activateDeactivateRoom(@NonNull UUID id, boolean active);
    RoomResponse getRoomDetail(@NonNull UUID id);
    List<RoomResponse> getRoomList();
}
