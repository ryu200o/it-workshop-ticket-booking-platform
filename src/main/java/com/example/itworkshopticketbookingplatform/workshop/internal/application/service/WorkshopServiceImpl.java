package com.example.itworkshopticketbookingplatform.workshop.internal.application.service;

import com.example.itworkshopticketbookingplatform.workshop.WorkshopRequest;
import com.example.itworkshopticketbookingplatform.workshop.WorkshopResponse;
import com.example.itworkshopticketbookingplatform.workshop.WorkshopService;
import com.example.itworkshopticketbookingplatform.workshop.internal.application.mapper.WorkshopMapper;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.event.*;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.Workshop;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopId;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopState;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.repository.WorkshopRepository;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.exception.InvalidWorkshopStateException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class WorkshopServiceImpl implements WorkshopService {

    private final WorkshopRepository workshopRepository;
    private final WorkshopMapper workshopMapper;
    private final ApplicationEventPublisher eventPublisher;

    public WorkshopServiceImpl(WorkshopRepository workshopRepository,
                               WorkshopMapper workshopMapper,
                               ApplicationEventPublisher eventPublisher) {
        this.workshopRepository = workshopRepository;
        this.workshopMapper = workshopMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public WorkshopResponse createDraft(WorkshopRequest request) {
        Workshop workshop = Workshop.createDraft(request.title(), request.description());
        Workshop saved = workshopRepository.save(workshop);
        return workshopMapper.toResponse(saved);
    }

    @Override
    public WorkshopResponse updateContent(String workshopId, WorkshopRequest request) {
        WorkshopId id = WorkshopId.of(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workshop not found: " + workshopId));
        
        workshop.updateContent(request.title(), request.description());
        Workshop saved = workshopRepository.save(workshop);
        return workshopMapper.toResponse(saved);
    }

    @Override
    public WorkshopResponse schedule(String workshopId, Instant startTime, Instant endTime, int capacity, String roomId) {
        WorkshopId id = WorkshopId.of(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workshop not found: " + workshopId));
        
        UUID roomUuid = UUID.fromString(roomId);
        // Note: Room display name snapshot would be fetched from Room module
        // For now, using roomId as placeholder - in real implementation, call RoomService
        String roomDisplayNameSnapshot = "Room " + roomId; // TODO: Fetch from Room module
        
        workshop.schedule(roomUuid, roomDisplayNameSnapshot, startTime, endTime, capacity);
        Workshop saved = workshopRepository.save(workshop);
        return workshopMapper.toResponse(saved);
    }

    @Override
    public WorkshopResponse publish(String workshopId) {
        WorkshopId id = WorkshopId.of(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workshop not found: " + workshopId));
        
        // Validate room conflict before publishing
        if (workshop.getRoomId() != null && workshop.getStartTime() != null && workshop.getEndTime() != null) {
            boolean conflict = workshopRepository.existsByRoomIdAndTimeRange(
                    workshop.getRoomId(), workshop.getStartTime(), workshop.getEndTime());
            if (conflict) {
                throw new IllegalStateException("Room conflict: Room is already booked for this time range");
            }
        }
        
        workshop.publish(
                workshop.getRoomId(),
                workshop.getRoomDisplayNameSnapshot(),
                workshop.getStartTime(),
                workshop.getEndTime(),
                workshop.getCapacity()
        );
        
        Workshop saved = workshopRepository.save(workshop);
        
        // Publish domain event
        eventPublisher.publishEvent(new WorkshopPublishedEvent(
                saved.getId(),
                saved.getTitle(),
                saved.getStartTime(),
                saved.getEndTime(),
                saved.getCapacity(),
                saved.getRoomId(),
                saved.getRoomDisplayNameSnapshot(),
                Instant.now()
        ));
        
        return workshopMapper.toResponse(saved);
    }

    @Override
    public WorkshopResponse reschedule(String workshopId, Instant startTime, Instant endTime, String roomId) {
        WorkshopId id = WorkshopId.of(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workshop not found: " + workshopId));
        
        UUID newRoomId = UUID.fromString(roomId);
        boolean roomChanged = !newRoomId.equals(workshop.getRoomId());
        
        // Validate room conflict for new schedule
        if (workshopRepository.existsByRoomIdAndTimeRangeExcluding(
                newRoomId, startTime, endTime, workshop.getId().value())) {
            throw new IllegalStateException("Room conflict: Room is already booked for this time range");
        }
        
        String newRoomDisplayNameSnapshot = "Room " + roomId; // TODO: Fetch from Room module
        
        Instant oldStartTime = workshop.getStartTime();
        Instant oldEndTime = workshop.getEndTime();
        
        workshop.reschedule(startTime, endTime, newRoomId, newRoomDisplayNameSnapshot, roomChanged);
        Workshop saved = workshopRepository.save(workshop);
        
        // Publish rescheduled event
        eventPublisher.publishEvent(new WorkshopRescheduledEvent(
                saved.getId(),
                oldStartTime,
                startTime,
                oldEndTime,
                endTime,
                roomChanged,
                newRoomDisplayNameSnapshot,
                Instant.now()
        ));
        
        // If room changed, publish room changed event
        if (roomChanged) {
            eventPublisher.publishEvent(new WorkshopRoomChangedEvent(
                    saved.getId(),
                    workshop.getRoomId(), // old room
                    newRoomId,
                    newRoomDisplayNameSnapshot,
                    Instant.now()
            ));
        }
        
        return workshopMapper.toResponse(saved);
    }

    @Override
    public WorkshopResponse start(String workshopId) {
        WorkshopId id = WorkshopId.of(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workshop not found: " + workshopId));
        
        workshop.start();
        Workshop saved = workshopRepository.save(workshop);
        
        eventPublisher.publishEvent(new WorkshopStartedEvent(
                saved.getId(),
                Instant.now()
        ));
        
        return workshopMapper.toResponse(saved);
    }

    @Override
    public WorkshopResponse complete(String workshopId) {
        WorkshopId id = WorkshopId.of(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workshop not found: " + workshopId));
        
        workshop.complete();
        Workshop saved = workshopRepository.save(workshop);
        
        eventPublisher.publishEvent(new WorkshopCompletedEvent(
                saved.getId(),
                Instant.now()
        ));
        
        return workshopMapper.toResponse(saved);
    }

    @Override
    public WorkshopResponse cancel(String workshopId) {
        WorkshopId id = WorkshopId.of(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workshop not found: " + workshopId));
        
        workshop.cancel();
        Workshop saved = workshopRepository.save(workshop);
        
        eventPublisher.publishEvent(new WorkshopCancelledEvent(
                saved.getId(),
                Instant.now(),
                "Cancelled by admin" // TODO: Add reason parameter
        ));
        
        return workshopMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkshopResponse findById(String workshopId) {
        WorkshopId id = WorkshopId.of(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workshop not found: " + workshopId));
        return workshopMapper.toResponse(workshop);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkshopResponse> findAll(Pageable pageable) {
        return workshopRepository.findAll(pageable)
                .map(workshopMapper::toResponse);
    }
}