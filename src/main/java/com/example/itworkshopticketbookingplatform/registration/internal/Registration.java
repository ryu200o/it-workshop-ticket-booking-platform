package com.example.itworkshopticketbookingplatform.registration.internal;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "registrations")
class Registration {

    enum Status {
        CONFIRMED, CANCELLED, ATTENDED, NO_SHOW
    }

    @Id
    private UUID id;

    @Column(name = "workshop_id", nullable = false)
    private UUID workshopId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Status status;

    @Column(name = "registration_time", nullable = false)
    private Instant registrationTime;

    @Column(name = "checked_in")
    private boolean checkedIn;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Column(name = "checked_in_by")
    private UUID checkedInBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Registration() {}

    Registration(UUID id, UUID workshopId, UUID userId, Instant registrationTime) {
        this.id = id;
        this.workshopId = workshopId;
        this.userId = userId;
        this.status = Status.CONFIRMED;
        this.registrationTime = registrationTime;
        this.checkedIn = false;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    // For persistence reconstruction
    Registration(UUID id, UUID workshopId, UUID userId, Status status, Instant registrationTime,
                 boolean checkedIn, Instant checkedInAt, UUID checkedInBy,
                 Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.workshopId = workshopId;
        this.userId = userId;
        this.status = status;
        this.registrationTime = registrationTime;
        this.checkedIn = checkedIn;
        this.checkedInAt = checkedInAt;
        this.checkedInBy = checkedInBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    void cancel() {
        if (this.status == Status.CANCELLED) {
            throw new RegistrationExceptions.InvalidRegistrationStateException("Registration is already cancelled");
        }
        if (this.status == Status.ATTENDED || this.status == Status.NO_SHOW) {
            throw new RegistrationExceptions.InvalidRegistrationStateException("Cannot cancel " + this.status + " registration");
        }
        this.status = Status.CANCELLED;
        this.updatedAt = Instant.now();
    }

    void checkIn(UUID checkedInBy) {
        if (this.status != Status.CONFIRMED) {
            throw new RegistrationExceptions.InvalidRegistrationStateException("Can only check-in CONFIRMED registrations, current: " + this.status);
        }
        this.checkedIn = true;
        this.checkedInAt = Instant.now();
        this.checkedInBy = checkedInBy;
        this.status = Status.ATTENDED;
        this.updatedAt = Instant.now();
    }

    void markNoShow() {
        if (this.status != Status.CONFIRMED) {
            throw new RegistrationExceptions.InvalidRegistrationStateException("Can only mark CONFIRMED as no-show, current: " + this.status);
        }
        this.status = Status.NO_SHOW;
        this.updatedAt = Instant.now();
    }

    // Getters
    UUID getId() { return id; }
    UUID getWorkshopId() { return workshopId; }
    UUID getUserId() { return userId; }
    Status getStatus() { return status; }
    Instant getRegistrationTime() { return registrationTime; }
    boolean isCheckedIn() { return checkedIn; }
    Instant getCheckedInAt() { return checkedInAt; }
    UUID getCheckedInBy() { return checkedInBy; }
    Instant getCreatedAt() { return createdAt; }
    Instant getUpdatedAt() { return updatedAt; }
}
