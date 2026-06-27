package com.example.itworkshopticketbookingplatform.workshop.internal.domain.repository;

import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.Workshop;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface WorkshopRepository {

    Workshop save(Workshop workshop);

    Optional<Workshop> findById(WorkshopId id);

    Optional<Workshop> findById(UUID id);

    Page<Workshop> findAll(Pageable pageable);

    boolean existsByRoomIdAndTimeRange(UUID roomId, Instant startTime, Instant endTime);

    boolean existsByRoomIdAndTimeRangeExcluding(UUID roomId, Instant startTime, Instant endTime, UUID excludeWorkshopId);

    void deleteById(WorkshopId id);
}