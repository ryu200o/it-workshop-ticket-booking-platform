package com.example.itworkshopticketbookingplatform.workshop.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

interface WorkshopSnapshotRepository extends JpaRepository<WorkshopSnapshot, UUID> {
    Optional<WorkshopSnapshot> findByWorkshopId(UUID workshopId);
}
