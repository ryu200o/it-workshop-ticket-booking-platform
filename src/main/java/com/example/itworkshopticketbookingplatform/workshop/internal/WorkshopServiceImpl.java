package com.example.itworkshopticketbookingplatform.workshop.internal;

import com.example.itworkshopticketbookingplatform.workshop.WorkshopEvents;
import com.example.itworkshopticketbookingplatform.workshop.WorkshopNotFoundException;
import com.example.itworkshopticketbookingplatform.workshop.dto.WorkshopRequest;
import com.example.itworkshopticketbookingplatform.workshop.dto.WorkshopResponse;
import com.example.itworkshopticketbookingplatform.workshop.WorkshopService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
class WorkshopServiceImpl implements WorkshopService {

    private final WorkshopRepository workshopRepository;
    private final ApplicationEventPublisher eventPublisher;

    WorkshopServiceImpl(WorkshopRepository workshopRepository,
                         ApplicationEventPublisher eventPublisher) {
        this.workshopRepository = workshopRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public WorkshopResponse createDraft(WorkshopRequest request) {
        Workshop workshop = Workshop.createDraft(request.title(), request.description());
        Workshop saved = workshopRepository.save(workshop);
        return toResponse(saved);
    }

    @Override
    public WorkshopResponse updateContent(String workshopId, WorkshopRequest request) {
        UUID id = UUID.fromString(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new WorkshopNotFoundException(UUID.fromString(workshopId)));

        workshop.updateContent(request.title(), request.description());
        Workshop saved = workshopRepository.save(workshop);
        return toResponse(saved);
    }

    @Override
    public WorkshopResponse schedule(String workshopId, Instant startTime, Instant endTime, int capacity, String roomId) {
        UUID id = UUID.fromString(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new WorkshopNotFoundException(UUID.fromString(workshopId)));

        UUID roomUuid = UUID.fromString(roomId);
        String roomDisplayNameSnapshot = "Room " + roomId; // TODO: Fetch from Room module

        workshop.schedule(roomUuid, roomDisplayNameSnapshot, startTime, endTime, capacity);
        Workshop saved = workshopRepository.save(workshop);
        return toResponse(saved);
    }

    @Override
    public WorkshopResponse publish(String workshopId) {
        UUID id = UUID.fromString(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new WorkshopNotFoundException(UUID.fromString(workshopId)));

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
        eventPublisher.publishEvent(new WorkshopEvents.Published(
                saved.getId(),
                saved.getTitle(),
                saved.getStartTime(),
                saved.getEndTime(),
                saved.getCapacity(),
                saved.getRoomId(),
                saved.getRoomDisplayNameSnapshot(),
                Instant.now()
        ));

        return toResponse(saved);
    }

    @Override
    public WorkshopResponse reschedule(String workshopId, Instant startTime, Instant endTime, String roomId) {
        UUID id = UUID.fromString(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new WorkshopNotFoundException(UUID.fromString(workshopId)));

        UUID newRoomId = UUID.fromString(roomId);
        boolean roomChanged = !newRoomId.equals(workshop.getRoomId());

        // Validate room conflict for new schedule
        if (workshopRepository.existsByRoomIdAndTimeRangeExcluding(
                newRoomId, startTime, endTime, workshop.getId())) {
            throw new IllegalStateException("Room conflict: Room is already booked for this time range");
        }

        String newRoomDisplayNameSnapshot = "Room " + roomId; // TODO: Fetch from Room module

        Instant oldStartTime = workshop.getStartTime();
        Instant oldEndTime = workshop.getEndTime();

        workshop.reschedule(startTime, endTime, newRoomId, newRoomDisplayNameSnapshot, roomChanged);
        Workshop saved = workshopRepository.save(workshop);

        // Publish rescheduled event
        eventPublisher.publishEvent(new WorkshopEvents.Rescheduled(
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
            eventPublisher.publishEvent(new WorkshopEvents.RoomChanged(
                    saved.getId(),
                    workshop.getRoomId(), // old room
                    newRoomId,
                    newRoomDisplayNameSnapshot,
                    Instant.now()
            ));
        }

        return toResponse(saved);
    }

    @Override
    public WorkshopResponse start(String workshopId) {
        UUID id = UUID.fromString(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new WorkshopNotFoundException(UUID.fromString(workshopId)));

        workshop.start();
        Workshop saved = workshopRepository.save(workshop);

        eventPublisher.publishEvent(new WorkshopEvents.Started(
                saved.getId(),
                Instant.now()
        ));

        return toResponse(saved);
    }

    @Override
    public WorkshopResponse complete(String workshopId) {
        UUID id = UUID.fromString(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new WorkshopNotFoundException(UUID.fromString(workshopId)));

        workshop.complete();
        Workshop saved = workshopRepository.save(workshop);

        eventPublisher.publishEvent(new WorkshopEvents.Completed(
                saved.getId(),
                Instant.now()
        ));

        return toResponse(saved);
    }

    @Override
    public WorkshopResponse cancel(String workshopId) {
        UUID id = UUID.fromString(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new WorkshopNotFoundException(UUID.fromString(workshopId)));

        workshop.cancel();
        Workshop saved = workshopRepository.save(workshop);

        eventPublisher.publishEvent(new WorkshopEvents.Cancelled(
                saved.getId(),
                "Cancelled by admin", // TODO: Add reason parameter
                Instant.now()
        ));

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkshopResponse findById(String workshopId) {
        UUID id = UUID.fromString(workshopId);
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new WorkshopNotFoundException(UUID.fromString(workshopId)));
        return toResponse(workshop);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkshopResponse> findAll(Pageable pageable) {
        return workshopRepository.findAll(pageable)
                .map(this::toResponse);
    }

    private WorkshopResponse toResponse(Workshop workshop) {
        return new WorkshopResponse(
                workshop.getId(),
                workshop.getTitle(),
                workshop.getDescription(),
                workshop.getRoomId(),
                workshop.getRoomDisplayNameSnapshot(),
                workshop.getStartTime(),
                workshop.getEndTime(),
                workshop.getCapacity(),
                workshop.getState().name(),
                workshop.getCreatedAt(),
                workshop.getUpdatedAt()
        );
    }
}