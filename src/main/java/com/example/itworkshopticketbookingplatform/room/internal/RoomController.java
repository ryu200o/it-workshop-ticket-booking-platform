package com.example.itworkshopticketbookingplatform.room.internal;

import com.example.itworkshopticketbookingplatform.room.dto.RoomActivationRequest;
import com.example.itworkshopticketbookingplatform.room.dto.RoomRequest;
import com.example.itworkshopticketbookingplatform.room.dto.RoomResponse;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms")
class RoomController {

    private final RoomService roomService;

    RoomController(@NonNull RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody @NonNull RoomRequest roomRequest) {
        RoomResponse roomResponse = roomService.createRoom(
                roomRequest.roomCode(),
                roomRequest.physicalCapacity(),
                roomRequest.location()
        );
        URI location = UriComponentsBuilder.fromPath("/api/v1/rooms/{id}")
                .buildAndExpand(roomResponse.id()).toUri();
        return ResponseEntity.created(location).body(roomResponse);
    }

    @PutMapping("/{roomId}")
    ResponseEntity<RoomResponse> updateRoom(@PathVariable @NonNull UUID roomId, @Valid @RequestBody @NonNull RoomRequest roomRequest) {
        RoomResponse roomResponse = roomService.updateRoom(
                roomId,
                roomRequest.roomCode(),
                roomRequest.physicalCapacity(),
                roomRequest.location()
        );
        return ResponseEntity.ok(roomResponse);
    }

    @PatchMapping("/{roomId}/activation")
    ResponseEntity<RoomResponse> activateDeactivateRoom(@PathVariable @NonNull UUID roomId, @Valid @RequestBody @NonNull RoomActivationRequest activationRequest) {
        RoomResponse roomResponse = roomService.activateDeactivateRoom(roomId, activationRequest.active());
        return ResponseEntity.ok(roomResponse);
    }

    @GetMapping
    ResponseEntity<List<RoomResponse>> getRoomList() {
        List<RoomResponse> roomResponses = roomService.getRoomList();
        return ResponseEntity.ok(roomResponses);
    }

    @GetMapping("/{roomId}")
    ResponseEntity<RoomResponse> getRoomDetail(@PathVariable @NonNull UUID roomId) {
        RoomResponse roomResponse = roomService.getRoomDetail(roomId);
        return ResponseEntity.ok(roomResponse);
    }
}
