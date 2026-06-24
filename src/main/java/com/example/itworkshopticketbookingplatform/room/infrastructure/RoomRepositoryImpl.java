package com.example.itworkshopticketbookingplatform.room.infrastructure;

import com.example.itworkshopticketbookingplatform.room.domain.Room;
import com.example.itworkshopticketbookingplatform.room.domain.RoomRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RoomRepositoryImpl implements RoomRepository {

    private final RoomJpaRepository roomJpaRepository;

    public RoomRepositoryImpl(@NonNull RoomJpaRepository roomJpaRepository) {
        this.roomJpaRepository = roomJpaRepository;
    }

    @Override
    public Room save(@NonNull Room room) {
        return roomJpaRepository.save(room);
    }

    @Override
    public Optional<Room> findById(@NonNull UUID id) {
        return roomJpaRepository.findById(id);
    }

    @Override
    public Optional<Room> findByRoomCode(@NonNull String roomCode) {
        return roomJpaRepository.findByRoomCode(roomCode);
    }

    @Override
    public List<Room> findAll() {
        return roomJpaRepository.findAll();
    }
}
