package com.example.itworkshopticketbookingplatform.room.internal;

import com.example.itworkshopticketbookingplatform.room.RoomService;
import com.example.itworkshopticketbookingplatform.room.RoomNotFoundException;
import com.example.itworkshopticketbookingplatform.room.internal.RoomExceptions.DuplicateRoomCodeException;
import com.example.itworkshopticketbookingplatform.room.dto.RoomResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    RoomServiceImpl(@NonNull RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public RoomResponse createRoom(@NonNull String roomCode, int physicalCapacity, @NonNull String location) {
        roomRepository.findByRoomCode(roomCode).ifPresent(ignored -> {
            throw new DuplicateRoomCodeException(roomCode);
        });

        Room newRoom = new Room(UUID.randomUUID(), roomCode, physicalCapacity, location);
        Room savedRoom = roomRepository.save(newRoom);
        return toResponse(savedRoom);
    }

    @Override
    public RoomResponse updateRoom(@NonNull UUID id, @NonNull String roomCode, int physicalCapacity, @NonNull String location) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));

        if (!room.getRoomCode().equals(roomCode)) {
            roomRepository.findByRoomCode(roomCode).ifPresent(ignored -> {
                throw new DuplicateRoomCodeException(roomCode);
            });
        }

        room.rename(roomCode);
        room.changeCapacity(physicalCapacity);
        room.changeLocation(location);

        Room updatedRoom = roomRepository.save(room);
        return toResponse(updatedRoom);
    }

    @Override
    public RoomResponse activateDeactivateRoom(@NonNull UUID id, boolean active) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        if (active) {
            room.activate();
        } else {
            room.deactivate();
        }
        Room updatedRoom = roomRepository.save(room);
        return toResponse(updatedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomDetail(@NonNull UUID id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        return toResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomList() {
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream().map(this::toResponse).toList();
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(room.getId(), room.getRoomCode(), room.getPhysicalCapacity(),
                                room.getLocation(), room.isActive(), room.getCreatedAt(), room.getUpdatedAt());
    }
}