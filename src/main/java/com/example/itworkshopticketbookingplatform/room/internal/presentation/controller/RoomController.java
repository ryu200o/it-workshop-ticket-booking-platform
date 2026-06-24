package com.example.itworkshopticketbookingplatform.room.internal.presentation.controller;

import com.example.itworkshopticketbookingplatform.room.RoomService;
import com.example.itworkshopticketbookingplatform.room.RoomActivationRequest;
import com.example.itworkshopticketbookingplatform.room.RoomRequest;
import com.example.itworkshopticketbookingplatform.room.RoomResponse;

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
public class RoomController {

    private final RoomService roomService;

    public RoomController(@NonNull RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody @NonNull RoomRequest roomRequest) {
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
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable @NonNull UUID roomId, @Valid @RequestBody @NonNull RoomRequest roomRequest) {
        RoomResponse roomResponse = roomService.updateRoom(
                roomId,
                roomRequest.roomCode(),
                roomRequest.physicalCapacity(),
                roomRequest.location()
        );
        return ResponseEntity.ok(roomResponse);
    }

    @PatchMapping("/{roomId}/activation")
    public ResponseEntity<RoomResponse> activateDeactivateRoom(@PathVariable @NonNull UUID roomId, @Valid @RequestBody @NonNull RoomActivationRequest activationRequest) {
        RoomResponse roomResponse = roomService.activateDeactivateRoom(roomId, activationRequest.active());
        return ResponseEntity.ok(roomResponse);
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getRoomList() {
        List<RoomResponse> roomResponses = roomService.getRoomList();
        return ResponseEntity.ok(roomResponses);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoomDetail(@PathVariable @NonNull UUID roomId) {
        RoomResponse roomResponse = roomService.getRoomDetail(roomId);
        return ResponseEntity.ok(roomResponse);
    }
}
