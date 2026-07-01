package com.example.itworkshopticketbookingplatform.workshop.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

interface WorkshopHistoryRepository extends JpaRepository<WorkshopHistory, UUID> {
    List<WorkshopHistory> findByWorkshopIdOrderByOccurredAtDesc(UUID workshopId);
}
