package com.example.itworkshopticketbookingplatform.workshop.internal;

import com.example.itworkshopticketbookingplatform.room.RoomEvents;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.modulith.events.ApplicationModuleListener;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
class RoomEventHandler {

    private final WorkshopRepository workshopRepository;
    private final WorkshopHistoryRepository workshopHistoryRepository;

    RoomEventHandler(WorkshopRepository workshopRepository,
                     WorkshopHistoryRepository workshopHistoryRepository) {
        this.workshopRepository = workshopRepository;
        this.workshopHistoryRepository = workshopHistoryRepository;
    }

    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRoomRenamed(RoomEvents.RoomRenamed event) {
        var workshops = workshopRepository.findByRoomId(event.roomId());
        for (var workshop : workshops) {
            if (workshop.getState() == WorkshopState.PUBLISHED) {
                workshop.updateRoomDisplayNameSnapshot(event.newName());
                workshopRepository.save(workshop);

                workshopHistoryRepository.save(new WorkshopHistory(
                    UUID.randomUUID(), workshop.getId(), "ROOM_RENAMED",
                    Map.of("oldName", event.oldName(), "newName", event.newName()),
                    "Room renamed", UUID.randomUUID(), Instant.now()
                ));
            } else if (workshop.getState() == WorkshopState.IN_PROGRESS) {
                workshopHistoryRepository.save(new WorkshopHistory(
                    UUID.randomUUID(), workshop.getId(), "ROOM_RENAMED_DURING_SESSION",
                    Map.of("oldName", event.oldName(), "newName", event.newName()),
                    "Room renamed while workshop in progress", UUID.randomUUID(), Instant.now()
                ));
            }
        }
    }

    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRoomLocationChanged(RoomEvents.RoomLocationChanged event) {
        var workshops = workshopRepository.findByRoomId(event.roomId());
        for (var workshop : workshops) {
            if (workshop.getState() == WorkshopState.PUBLISHED) {
                workshopHistoryRepository.save(new WorkshopHistory(
                    UUID.randomUUID(), workshop.getId(), "ROOM_LOCATION_CHANGED",
                    Map.of("oldLocation", event.oldLocation(), "newLocation", event.newLocation()),
                    "Room location changed — reschedule required", UUID.randomUUID(), Instant.now()
                ));
            } else if (workshop.getState() == WorkshopState.IN_PROGRESS) {
                workshopHistoryRepository.save(new WorkshopHistory(
                    UUID.randomUUID(), workshop.getId(), "ROOM_LOCATION_CHANGED_EMERGENCY",
                    Map.of("oldLocation", event.oldLocation(), "newLocation", event.newLocation()),
                    "EMERGENCY: Room location changed while workshop in progress",
                    UUID.randomUUID(), Instant.now()
                ));
            }
        }
    }

    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRoomDeactivated(RoomEvents.RoomDeactivated event) {
        var workshops = workshopRepository.findByRoomId(event.roomId());
        for (var workshop : workshops) {
            if (workshop.getState() == WorkshopState.PUBLISHED ||
                workshop.getState() == WorkshopState.IN_PROGRESS) {
                workshopHistoryRepository.save(new WorkshopHistory(
                    UUID.randomUUID(), workshop.getId(), "ROOM_DEACTIVATED",
                    Map.of("wasActive", event.wasActive()),
                    "Room deactivated — workshop affected", UUID.randomUUID(), Instant.now()
                ));
            }
        }
    }
}
