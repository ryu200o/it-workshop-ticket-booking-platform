package com.example.itworkshopticketbookingplatform.workshop.internal.infrastructure.persistence.repository;

import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.Workshop;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopId;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopState;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.repository.WorkshopRepository;
import com.example.itworkshopticketbookingplatform.workshop.internal.infrastructure.persistence.jpa.WorkshopJpaEntity;
import com.example.itworkshopticketbookingplatform.workshop.internal.infrastructure.persistence.jpa.WorkshopJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WorkshopRepositoryImpl implements WorkshopRepository {

    private final WorkshopJpaRepository jpaRepository;

    public WorkshopRepositoryImpl(WorkshopJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Workshop save(Workshop workshop) {
        WorkshopJpaEntity entity = toEntity(workshop);
        WorkshopJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Workshop> findById(WorkshopId id) {
        return jpaRepository.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public Optional<Workshop> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Page<Workshop> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByRoomIdAndTimeRange(UUID roomId, Instant startTime, Instant endTime) {
        return jpaRepository.existsByRoomIdAndTimeRange(roomId, startTime, endTime);
    }

    @Override
    public boolean existsByRoomIdAndTimeRangeExcluding(UUID roomId, Instant startTime, Instant endTime, UUID excludeWorkshopId) {
        return jpaRepository.existsByRoomIdAndTimeRangeExcluding(roomId, startTime, endTime, excludeWorkshopId);
    }

    @Override
    public void deleteById(WorkshopId id) {
        jpaRepository.deleteById(id.value());
    }

    private WorkshopJpaEntity toEntity(Workshop workshop) {
        return new WorkshopJpaEntity(
                workshop.getId().value(),
                workshop.getTitle(),
                workshop.getDescription(),
                workshop.getRoomId(),
                workshop.getRoomDisplayNameSnapshot(),
                workshop.getStartTime(),
                workshop.getEndTime(),
                workshop.getCapacity(),
                workshop.getState(),
                workshop.getCreatedAt(),
                workshop.getUpdatedAt()
        );
    }

    private Workshop toDomain(WorkshopJpaEntity entity) {
        return Workshop.fromPersistence(
                new WorkshopId(entity.getId()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getRoomId(),
                entity.getRoomDisplayNameSnapshot(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getCapacity(),
                entity.getState(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}