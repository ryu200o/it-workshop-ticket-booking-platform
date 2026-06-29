package com.example.itworkshopticketbookingplatform.workshop.internal;

import com.example.itworkshopticketbookingplatform.workshop.dto.WorkshopRequest;
import com.example.itworkshopticketbookingplatform.workshop.dto.WorkshopResponse;
import com.example.itworkshopticketbookingplatform.workshop.WorkshopService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/workshops")
class WorkshopController {

    private final WorkshopService workshopService;

    WorkshopController(WorkshopService workshopService) {
        this.workshopService = workshopService;
    }

    @PostMapping
    ResponseEntity<WorkshopResponse> createDraft(@Valid @RequestBody WorkshopRequest request) {
        WorkshopResponse response = workshopService.createDraft(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    ResponseEntity<WorkshopResponse> updateContent(
            @PathVariable String id,
            @Valid @RequestBody WorkshopRequest request) {
        WorkshopResponse response = workshopService.updateContent(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/schedule")
    ResponseEntity<WorkshopResponse> schedule(
            @PathVariable String id,
            @RequestParam Instant startTime,
            @RequestParam Instant endTime,
            @RequestParam int capacity,
            @RequestParam String roomId) {
        WorkshopResponse response = workshopService.schedule(id, startTime, endTime, capacity, roomId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/publish")
    ResponseEntity<WorkshopResponse> publish(@PathVariable String id) {
        WorkshopResponse response = workshopService.publish(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reschedule")
    ResponseEntity<WorkshopResponse> reschedule(
            @PathVariable String id,
            @RequestParam Instant startTime,
            @RequestParam Instant endTime,
            @RequestParam String roomId) {
        WorkshopResponse response = workshopService.reschedule(id, startTime, endTime, roomId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/start")
    ResponseEntity<WorkshopResponse> start(@PathVariable String id) {
        WorkshopResponse response = workshopService.start(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    ResponseEntity<WorkshopResponse> complete(@PathVariable String id) {
        WorkshopResponse response = workshopService.complete(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    ResponseEntity<WorkshopResponse> cancel(@PathVariable String id) {
        WorkshopResponse response = workshopService.cancel(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    ResponseEntity<WorkshopResponse> findById(@PathVariable String id) {
        WorkshopResponse response = workshopService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    ResponseEntity<Page<WorkshopResponse>> findAll(
            @Valid WorkshopPageRequest pageRequest) {
        Pageable pageable = pageRequest.toPageable();
        Page<WorkshopResponse> response = workshopService.findAll(pageable);
        return ResponseEntity.ok(response);
    }
}
