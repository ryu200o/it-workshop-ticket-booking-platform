package com.example.itworkshopticketbookingplatform.workshop.internal.presentation.controller;

import com.example.itworkshopticketbookingplatform.workshop.WorkshopRequest;
import com.example.itworkshopticketbookingplatform.workshop.WorkshopResponse;
import com.example.itworkshopticketbookingplatform.workshop.WorkshopService;
import com.example.itworkshopticketbookingplatform.workshop.internal.application.dto.WorkshopPageRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/workshops")
public class WorkshopController {

    private final WorkshopService workshopService;

    public WorkshopController(WorkshopService workshopService) {
        this.workshopService = workshopService;
    }

    @PostMapping
    public ResponseEntity<WorkshopResponse> createDraft(@Valid @RequestBody WorkshopRequest request) {
        WorkshopResponse response = workshopService.createDraft(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkshopResponse> updateContent(
            @PathVariable String id,
            @Valid @RequestBody WorkshopRequest request) {
        WorkshopResponse response = workshopService.updateContent(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/schedule")
    public ResponseEntity<WorkshopResponse> schedule(
            @PathVariable String id,
            @RequestParam Instant startTime,
            @RequestParam Instant endTime,
            @RequestParam int capacity,
            @RequestParam String roomId) {
        WorkshopResponse response = workshopService.schedule(id, startTime, endTime, capacity, roomId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<WorkshopResponse> publish(@PathVariable String id) {
        WorkshopResponse response = workshopService.publish(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<WorkshopResponse> reschedule(
            @PathVariable String id,
            @RequestParam Instant startTime,
            @RequestParam Instant endTime,
            @RequestParam String roomId) {
        WorkshopResponse response = workshopService.reschedule(id, startTime, endTime, roomId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<WorkshopResponse> start(@PathVariable String id) {
        WorkshopResponse response = workshopService.start(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<WorkshopResponse> complete(@PathVariable String id) {
        WorkshopResponse response = workshopService.complete(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<WorkshopResponse> cancel(@PathVariable String id) {
        WorkshopResponse response = workshopService.cancel(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkshopResponse> findById(@PathVariable String id) {
        WorkshopResponse response = workshopService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<WorkshopResponse>> findAll(
            @Valid WorkshopPageRequest pageRequest) {
        Pageable pageable = pageRequest.toPageable();
        Page<WorkshopResponse> response = workshopService.findAll(pageable);
        return ResponseEntity.ok(response);
    }
}