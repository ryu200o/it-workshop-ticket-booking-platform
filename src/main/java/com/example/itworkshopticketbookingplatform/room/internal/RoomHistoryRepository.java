package com.example.itworkshopticketbookingplatform.room.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

interface RoomHistoryRepository extends JpaRepository<RoomHistory, UUID> {
    List<RoomHistory> findByRoomIdOrderByChangedAtDesc(UUID roomId);
}
