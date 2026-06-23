package com.example.itworkshopticketbookingplatform.room.infrastructure;

import com.example.itworkshopticketbookingplatform.room.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RoomJpaRepository extends JpaRepository<Room, UUID> {
    Optional<Room> findByRoomCode(String roomCode);
}
