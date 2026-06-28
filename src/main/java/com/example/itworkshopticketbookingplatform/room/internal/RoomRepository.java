package com.example.itworkshopticketbookingplatform.room.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

interface RoomRepository extends JpaRepository<Room, UUID> {
    Optional<Room> findByRoomCode(String roomCode);
}