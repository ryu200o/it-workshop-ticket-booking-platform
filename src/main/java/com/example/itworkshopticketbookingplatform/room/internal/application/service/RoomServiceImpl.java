package com.example.itworkshopticketbookingplatform.room.internal.application.service;

import com.example.itworkshopticketbookingplatform.room.RoomService;
import com.example.itworkshopticketbookingplatform.room.RoomResponse;
import com.example.itworkshopticketbookingplatform.room.internal.application.mapper.RoomMapper;
import com.example.itworkshopticketbookingplatform.room.internal.domain.exception.DuplicateRoomCodeException;
import com.example.itworkshopticketbookingplatform.room.internal.domain.model.Room;
import com.example.itworkshopticketbookingplatform.room.internal.domain.exception.RoomNotFoundException;
import com.example.itworkshopticketbookingplatform.room.internal.domain.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public RoomServiceImpl(@NonNull RoomRepository roomRepository, @NonNull RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.roomMapper = roomMapper;
    }

    @Override
    public RoomResponse createRoom(@NonNull String roomCode, int physicalCapacity, @NonNull String location) {
        roomRepository.findByRoomCode(roomCode).ifPresent(ignored -> {
            throw new DuplicateRoomCodeException(roomCode);
        });

        Room newRoom = new Room(UUID.randomUUID(), roomCode, physicalCapacity, location);
        Room savedRoom = roomRepository.save(newRoom);
        return roomMapper.toRoomResponse(savedRoom);
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
        return roomMapper.toRoomResponse(updatedRoom);
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
        return roomMapper.toRoomResponse(updatedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomDetail(@NonNull UUID id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        return roomMapper.toRoomResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomList() {
        List<Room> rooms = roomRepository.findAll();
        return roomMapper.toRoomResponseList(rooms);
    }
}
