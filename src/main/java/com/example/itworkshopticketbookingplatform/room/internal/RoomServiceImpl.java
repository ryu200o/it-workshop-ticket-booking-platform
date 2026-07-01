package com.example.itworkshopticketbookingplatform.room.internal;

import com.example.itworkshopticketbookingplatform.room.RoomEvents;
import com.example.itworkshopticketbookingplatform.room.RoomNotFoundException;
import com.example.itworkshopticketbookingplatform.room.internal.RoomExceptions.DuplicateRoomCodeException;
import com.example.itworkshopticketbookingplatform.room.dto.RoomResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomHistoryRepository roomHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    RoomServiceImpl(@NonNull RoomRepository roomRepository,
                    @NonNull RoomHistoryRepository roomHistoryRepository,
                    @NonNull ApplicationEventPublisher eventPublisher) {
        this.roomRepository = roomRepository;
        this.roomHistoryRepository = roomHistoryRepository;
        this.eventPublisher = eventPublisher;
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

        // Track changes
        Map<String, Object> changes = new LinkedHashMap<>();
        if (!room.getRoomCode().equals(roomCode)) {
            changes.put("roomCode", Map.of("old", room.getRoomCode(), "new", roomCode));
        }
        if (room.getPhysicalCapacity() != physicalCapacity) {
            changes.put("physicalCapacity", Map.of("old", room.getPhysicalCapacity(), "new", physicalCapacity));
        }
        if (!room.getLocation().equals(location)) {
            changes.put("location", Map.of("old", room.getLocation(), "new", location));
        }

        if (!room.getRoomCode().equals(roomCode)) {
            roomRepository.findByRoomCode(roomCode).ifPresent(ignored -> {
                throw new DuplicateRoomCodeException(roomCode);
            });
        }

        boolean wasRenamed = !room.getRoomCode().equals(roomCode);
        room.rename(roomCode);
        room.changeCapacity(physicalCapacity);
        room.changeLocation(location);

        Room updatedRoom = roomRepository.save(room);

        // Save history
        if (!changes.isEmpty()) {
            roomHistoryRepository.save(new RoomHistory(
                    UUID.randomUUID(), id, Instant.now(), UUID.randomUUID(),
                    "Room updated", changes
            ));
        }

        // Publish events
        if (wasRenamed) {
            eventPublisher.publishEvent(new RoomEvents.RoomRenamed(
                    id, changes.containsKey("roomCode") ? (String) ((Map<?,?>) changes.get("roomCode")).get("old") : room.getRoomCode(),
                    roomCode, Instant.now()
            ));
        }
        if (changes.containsKey("location")) {
            eventPublisher.publishEvent(new RoomEvents.RoomLocationChanged(
                    id,
                    (String) ((Map<?,?>) changes.get("location")).get("old"),
                    location, Instant.now()
            ));
        }

        return toResponse(updatedRoom);
    }

    @Override
    public RoomResponse activateDeactivateRoom(@NonNull UUID id, boolean active) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));

        boolean wasActive = room.isActive();
        if (active) {
            room.activate();
        } else {
            room.deactivate();
        }
        Room updatedRoom = roomRepository.save(room);

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("active", Map.of("old", wasActive, "new", active));

        roomHistoryRepository.save(new RoomHistory(
                UUID.randomUUID(), id, Instant.now(), UUID.randomUUID(),
                active ? "Room activated" : "Room deactivated", changes
        ));

        if (!active) {
            eventPublisher.publishEvent(new RoomEvents.RoomDeactivated(id, wasActive, Instant.now()));
        }

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
