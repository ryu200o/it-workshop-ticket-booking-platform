package com.example.itworkshopticketbookingplatform.room.application;

import com.example.itworkshopticketbookingplatform.room.domain.DuplicateRoomCodeException;
import com.example.itworkshopticketbookingplatform.room.domain.Room;
import com.example.itworkshopticketbookingplatform.room.domain.RoomNotFoundException;
import com.example.itworkshopticketbookingplatform.room.domain.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room createRoom(String roomCode, int physicalCapacity, String location) {
        roomRepository.findByRoomCode(roomCode).ifPresent(room -> {
            throw new DuplicateRoomCodeException(roomCode);
        });

        Room newRoom = new Room(UUID.randomUUID(), roomCode, physicalCapacity, location);
        return roomRepository.save(newRoom);
    }

    public Room updateRoom(UUID id, String roomCode, int physicalCapacity, String location) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));

        if (!room.getRoomCode().equals(roomCode)) {
            roomRepository.findByRoomCode(roomCode).ifPresent(existingRoom -> {
                throw new DuplicateRoomCodeException(roomCode);
            });
        }

        room.rename(roomCode);
        room.changeCapacity(physicalCapacity);
        room.changeLocation(location);

        return roomRepository.save(room);
    }

    public Room activateDeactivateRoom(UUID id, boolean active) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        if (active) {
            room.activate();
        } else {
            room.deactivate();
        }
        return roomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public Room getRoomDetail(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Room> getRoomList() {
        return roomRepository.findAll();
    }
}
