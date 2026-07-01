package com.example.itworkshopticketbookingplatform.registration.internal;

import com.example.itworkshopticketbookingplatform.registration.dto.RegistrationRequest;
import com.example.itworkshopticketbookingplatform.registration.dto.RegistrationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/registrations")
class RegistrationController {

    private final RegistrationService registrationService;

    RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse response = registrationService.register(request.workshopId(), request.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/cancel")
    ResponseEntity<RegistrationResponse> cancel(@PathVariable UUID id) {
        RegistrationResponse response = registrationService.cancel(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/check-in")
    ResponseEntity<RegistrationResponse> checkIn(@PathVariable UUID id, @RequestParam UUID checkedInBy) {
        RegistrationResponse response = registrationService.checkIn(id, checkedInBy);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/no-show")
    ResponseEntity<RegistrationResponse> markNoShow(@PathVariable UUID id) {
        RegistrationResponse response = registrationService.markNoShow(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/workshop/{workshopId}")
    ResponseEntity<List<RegistrationResponse>> findByWorkshop(@PathVariable UUID workshopId) {
        List<RegistrationResponse> responses = registrationService.findByWorkshop(workshopId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}")
    ResponseEntity<List<RegistrationResponse>> findByUser(@PathVariable UUID userId) {
        List<RegistrationResponse> responses = registrationService.findByUser(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/workshop/{workshopId}/attendance")
    ResponseEntity<Long> getAttendance(@PathVariable UUID workshopId) {
        long count = registrationService.getAttendance(workshopId);
        return ResponseEntity.ok(count);
    }
}
