package com.example.itworkshopticketbookingplatform.room.presentation;

import com.example.itworkshopticketbookingplatform.room.application.RoomService;
import com.example.itworkshopticketbookingplatform.room.domain.Room;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest roomRequest) {
        Room room = roomService.createRoom(
                roomRequest.roomCode(),
                roomRequest.physicalCapacity(),
                roomRequest.location()
        );
        URI location = UriComponentsBuilder.fromPath("/api/v1/rooms/{id}")
                .buildAndExpand(room.getId()).toUri();
        return ResponseEntity.created(location).body(new RoomResponse(room));
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable UUID roomId, @Valid @RequestBody RoomRequest roomRequest) {
        Room room = roomService.updateRoom(
                roomId,
                roomRequest.roomCode(),
                roomRequest.physicalCapacity(),
                roomRequest.location()
        );
        return ResponseEntity.ok(new RoomResponse(room));
    }

    @PatchMapping("/{roomId}/activation")
    public ResponseEntity<Void> activateDeactivateRoom(@PathVariable UUID roomId, @Valid @RequestBody RoomActivationRequest activationRequest) {
        roomService.activateDeactivateRoom(roomId, activationRequest.active());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getRoomList() {
        List<RoomResponse> roomResponses = roomService.getRoomList().stream()
                .map(RoomResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roomResponses);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoomDetail(@PathVariable UUID roomId) {
        Room room = roomService.getRoomDetail(roomId);
        return ResponseEntity.ok(new RoomResponse(room));
    }
}
