package com.example.itworkshopticketbookingplatform.room.internal.infrastructure.persistence.repository;

import com.example.itworkshopticketbookingplatform.room.internal.domain.model.Room;
import com.example.itworkshopticketbookingplatform.room.internal.domain.repository.RoomRepository;
import com.example.itworkshopticketbookingplatform.room.internal.infrastructure.persistence.jpa.RoomJpaEntity;
import com.example.itworkshopticketbookingplatform.room.internal.infrastructure.persistence.jpa.RoomJpaRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class RoomRepositoryImpl implements RoomRepository {

    private final RoomJpaRepository roomJpaRepository;

    public RoomRepositoryImpl(@NonNull RoomJpaRepository roomJpaRepository) {
        this.roomJpaRepository = roomJpaRepository;
    }

    @Override
    public Room save(@NonNull Room room) {
        RoomJpaEntity jpaEntity = toJpaEntity(room);
        RoomJpaEntity savedEntity = roomJpaRepository.save(jpaEntity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Room> findById(@NonNull UUID id) {
        return roomJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Room> findByRoomCode(@NonNull String roomCode) {
        return roomJpaRepository.findByRoomCode(roomCode).map(this::toDomain);
    }

    @Override
    public List<Room> findAll() {
        return roomJpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Room toDomain(RoomJpaEntity entity) {
        if (entity == null) return null;
        return new Room(
            entity.getId(),
            entity.getRoomCode(),
            entity.getPhysicalCapacity(),
            entity.getLocation(),
            entity.isActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private RoomJpaEntity toJpaEntity(Room domain) {
        if (domain == null) return null;
        return new RoomJpaEntity(
            domain.getId(),
            domain.getRoomCode(),
            domain.getPhysicalCapacity(),
            domain.getLocation(),
            domain.isActive(),
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }
}
