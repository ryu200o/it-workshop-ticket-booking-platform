package com.example.itworkshopticketbookingplatform.room.domain;

import org.jspecify.annotations.NonNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository {
    Room save(@NonNull Room room);
    Optional<Room> findById(@NonNull UUID id);
    Optional<Room> findByRoomCode(@NonNull String roomCode);
    List<Room> findAll();
}