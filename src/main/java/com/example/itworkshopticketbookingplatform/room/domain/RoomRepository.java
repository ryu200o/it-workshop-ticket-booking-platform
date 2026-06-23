package com.example.itworkshopticketbookingplatform.room.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository {
    Room save(Room room);
    Optional<Room> findById(UUID id);
    Optional<Room> findByRoomCode(String roomCode);
    List<Room> findAll();
}