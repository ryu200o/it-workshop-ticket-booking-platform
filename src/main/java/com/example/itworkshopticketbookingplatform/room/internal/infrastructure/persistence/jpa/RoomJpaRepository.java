package com.example.itworkshopticketbookingplatform.room.internal.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RoomJpaRepository extends JpaRepository<RoomJpaEntity, UUID> {
    Optional<RoomJpaEntity> findByRoomCode(String roomCode);
}
