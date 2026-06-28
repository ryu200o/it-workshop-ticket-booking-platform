package com.example.itworkshopticketbookingplatform.workshop.internal;

import com.example.itworkshopticketbookingplatform.workshop.WorkshopRequest;
import com.example.itworkshopticketbookingplatform.workshop.WorkshopResponse;
import com.example.itworkshopticketbookingplatform.workshop.WorkshopEvents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkshopServiceImpl Tests")
class WorkshopServiceIntegrationTests {

    @Mock
    private WorkshopRepository workshopRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WorkshopServiceImpl workshopService;

    @Captor
    private ArgumentCaptor<Workshop> workshopCaptor;

    private static final String WORKSHOP_ID = UUID.randomUUID().toString();
    private static final UUID WORKSHOP_UUID = UUID.fromString(WORKSHOP_ID);
    private static final String ROOM_ID = UUID.randomUUID().toString();
    private static final UUID ROOM_UUID = UUID.fromString(ROOM_ID);
    private static final Instant START_TIME = Instant.now().plusSeconds(3600);
    private static final Instant END_TIME = Instant.now().plusSeconds(7200);
    private static final int CAPACITY = 50;

    private Workshop draftWorkshop;
    private Workshop publishedWorkshop;
    private Workshop inProgressWorkshop;
    private WorkshopResponse sampleResponse;

    @BeforeEach
    void setUp() {
        draftWorkshop = Workshop.createDraft("Draft Title", "Draft Description");
        // Use reflection to set the ID for consistency
        setWorkshopId(draftWorkshop, WORKSHOP_UUID);

        publishedWorkshop = Workshop.createDraft("Published Title", "Published Description");
        setWorkshopId(publishedWorkshop, WORKSHOP_UUID);
        publishedWorkshop.schedule(ROOM_UUID, "Room A", START_TIME, END_TIME, CAPACITY);
        publishedWorkshop.publish(publishedWorkshop.getRoomId(), publishedWorkshop.getRoomDisplayNameSnapshot(),
                publishedWorkshop.getStartTime(), publishedWorkshop.getEndTime(), publishedWorkshop.getCapacity());

        inProgressWorkshop = Workshop.createDraft("In Progress Title", "IP Description");
        setWorkshopId(inProgressWorkshop, WORKSHOP_UUID);
        inProgressWorkshop.schedule(ROOM_UUID, "Room A", START_TIME, END_TIME, CAPACITY);
        inProgressWorkshop.publish(inProgressWorkshop.getRoomId(), inProgressWorkshop.getRoomDisplayNameSnapshot(),
                inProgressWorkshop.getStartTime(), inProgressWorkshop.getEndTime(), inProgressWorkshop.getCapacity());
        inProgressWorkshop.start();

        sampleResponse = new WorkshopResponse(
                WORKSHOP_UUID, "Title", "Description",
                ROOM_UUID, "Room A", START_TIME, END_TIME, CAPACITY,
                "DRAFT", Instant.now(), Instant.now()
        );
    }

    private void setWorkshopId(Workshop workshop, UUID id) {
        // Use reflection to set the final id field for testing
        try {
            var field = Workshop.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(workshop, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ================================================================
    //  CREATE DRAFT
    // ================================================================

    @Nested
    @DisplayName("createDraft")
    class CreateDraftTests {

        @Test
        @DisplayName("should create draft workshop and persist it")
        void shouldCreateDraftAndPersist() {
            var request = new WorkshopRequest("New Workshop", "New description");
            when(workshopRepository.save(any(Workshop.class))).thenReturn(draftWorkshop);

            var result = workshopService.createDraft(request);

            assertNotNull(result);
            verify(workshopRepository).save(workshopCaptor.capture());
            var saved = workshopCaptor.getValue();
            assertEquals("New Workshop", saved.getTitle());
            assertEquals("New description", saved.getDescription());
            assertEquals(WorkshopState.DRAFT, saved.getState());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("should throw when repository fails")
        void shouldThrowWhenRepositoryFails() {
            var request = new WorkshopRequest("Title", "Description");
            when(workshopRepository.save(any(Workshop.class))).thenThrow(new RuntimeException("DB error"));

            assertThrows(RuntimeException.class, () -> workshopService.createDraft(request));
        }
    }

    // ================================================================
    //  UPDATE CONTENT
    // ================================================================

    @Nested
    @DisplayName("updateContent")
    class UpdateContentTests {

        @Test
        @DisplayName("should update content of draft workshop")
        void shouldUpdateContentInDraft() {
            var request = new WorkshopRequest("Updated Title", "Updated description");
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(draftWorkshop));
            when(workshopRepository.save(any(Workshop.class))).thenReturn(draftWorkshop);

            var result = workshopService.updateContent(WORKSHOP_ID, request);

            assertNotNull(result);
            verify(workshopRepository).save(workshopCaptor.capture());
            var saved = workshopCaptor.getValue();
            assertEquals("Updated Title", saved.getTitle());
            assertEquals("Updated description", saved.getDescription());
            assertEquals(WorkshopState.DRAFT, saved.getState());
        }

        @Test
        @DisplayName("should update content of published workshop")
        void shouldUpdateContentInPublished() {
            var request = new WorkshopRequest("Updated Title", "Updated description");
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(publishedWorkshop));
            when(workshopRepository.save(any(Workshop.class))).thenReturn(publishedWorkshop);

            var result = workshopService.updateContent(WORKSHOP_ID, request);

            assertNotNull(result);
            verify(workshopRepository).save(any(Workshop.class));
            assertEquals(WorkshopState.PUBLISHED, publishedWorkshop.getState());
        }

        @Test
        @DisplayName("should throw when workshop not found")
        void shouldThrowWhenNotFound() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.empty());
            var request = new WorkshopRequest("Title", "Description");

            assertThrows(IllegalArgumentException.class,
                    () -> workshopService.updateContent(WORKSHOP_ID, request));
        }

        @Test
        @DisplayName("should throw when updating content of in-progress workshop")
        void shouldThrowWhenInProgress() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(inProgressWorkshop));
            var request = new WorkshopRequest("Title", "Description");

            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshopService.updateContent(WORKSHOP_ID, request));
        }
    }

    // ================================================================
    //  SCHEDULE
    // ================================================================

    @Nested
    @DisplayName("schedule")
    class ScheduleTests {

        @Test
        @DisplayName("should schedule draft workshop")
        void shouldScheduleDraftWorkshop() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(draftWorkshop));
            when(workshopRepository.save(any(Workshop.class))).thenReturn(draftWorkshop);

            var result = workshopService.schedule(WORKSHOP_ID, START_TIME, END_TIME, CAPACITY, ROOM_ID);

            assertNotNull(result);
            verify(workshopRepository).save(any(Workshop.class));
            assertEquals(ROOM_UUID, draftWorkshop.getRoomId());
            assertEquals(START_TIME, draftWorkshop.getStartTime());
            assertEquals(CAPACITY, draftWorkshop.getCapacity());
        }

        @Test
        @DisplayName("should throw when workshop not found")
        void shouldThrowWhenNotFound() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> workshopService.schedule(WORKSHOP_ID, START_TIME, END_TIME, CAPACITY, ROOM_ID));
        }
    }

    // ================================================================
    //  PUBLISH
    // ================================================================

    @Nested
    @DisplayName("publish")
    class PublishTests {

        @Test
        @DisplayName("should publish scheduled draft workshop and emit event")
        void shouldPublishAndEmitEvent() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(draftWorkshop));
            when(workshopRepository.save(any(Workshop.class))).thenReturn(draftWorkshop);

            workshopService.schedule(WORKSHOP_ID, START_TIME, END_TIME, CAPACITY, ROOM_ID);

            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(draftWorkshop));
            when(workshopRepository.existsByRoomIdAndTimeRange(any(), any(), any())).thenReturn(false);
            when(workshopRepository.save(any(Workshop.class))).thenReturn(draftWorkshop);

            var result = workshopService.publish(WORKSHOP_ID);

            assertNotNull(result);
            assertEquals(WorkshopState.PUBLISHED, draftWorkshop.getState());
            verify(eventPublisher).publishEvent(any(WorkshopEvents.Published.class));
        }

        @Test
        @DisplayName("should throw when room conflict detected")
        void shouldThrowOnRoomConflict() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(draftWorkshop));
            when(workshopRepository.save(any(Workshop.class))).thenReturn(draftWorkshop);

            workshopService.schedule(WORKSHOP_ID, START_TIME, END_TIME, CAPACITY, ROOM_ID);

            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(draftWorkshop));
            when(workshopRepository.existsByRoomIdAndTimeRange(any(), any(), any())).thenReturn(true);

            assertThrows(IllegalStateException.class,
                    () -> workshopService.publish(WORKSHOP_ID));
        }

        @Test
        @DisplayName("should throw when workshop not found")
        void shouldThrowWhenNotFound() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> workshopService.publish(WORKSHOP_ID));
        }
    }

    // ================================================================
    //  RESCHEDULE
    // ================================================================

    @Nested
    @DisplayName("reschedule")
    class RescheduleTests {

        @Test
        @DisplayName("should reschedule published workshop and emit events")
        void shouldRescheduleAndEmitEvents() {
            UUID newRoomId = UUID.randomUUID();
            String newRoomIdStr = newRoomId.toString();
            Instant newStart = Instant.now().plusSeconds(10000);
            Instant newEnd = Instant.now().plusSeconds(14000);

            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(publishedWorkshop));
            when(workshopRepository.existsByRoomIdAndTimeRangeExcluding(any(), any(), any(), any())).thenReturn(false);
            when(workshopRepository.save(any(Workshop.class))).thenReturn(publishedWorkshop);

            var result = workshopService.reschedule(WORKSHOP_ID, newStart, newEnd, newRoomIdStr);

            assertNotNull(result);
            verify(eventPublisher).publishEvent(any(WorkshopEvents.Rescheduled.class));
            verify(eventPublisher).publishEvent(any(WorkshopEvents.RoomChanged.class));
        }

        @Test
        @DisplayName("should NOT emit room changed event when room unchanged")
        void shouldNotEmitRoomChangedWhenSameRoom() {
            Instant newStart = Instant.now().plusSeconds(10000);
            Instant newEnd = Instant.now().plusSeconds(14000);

            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(publishedWorkshop));
            when(workshopRepository.existsByRoomIdAndTimeRangeExcluding(any(), any(), any(), any())).thenReturn(false);
            when(workshopRepository.save(any(Workshop.class))).thenReturn(publishedWorkshop);

            workshopService.reschedule(WORKSHOP_ID, newStart, newEnd, ROOM_ID);

            verify(eventPublisher).publishEvent(any(WorkshopEvents.Rescheduled.class));
            verify(eventPublisher, never()).publishEvent(any(WorkshopEvents.RoomChanged.class));
        }

        @Test
        @DisplayName("should throw on room conflict")
        void shouldThrowOnRoomConflict() {
            Instant newStart = Instant.now().plusSeconds(10000);
            Instant newEnd = Instant.now().plusSeconds(14000);

            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(publishedWorkshop));
            when(workshopRepository.existsByRoomIdAndTimeRangeExcluding(any(), any(), any(), any())).thenReturn(true);

            assertThrows(IllegalStateException.class,
                    () -> workshopService.reschedule(WORKSHOP_ID, newStart, newEnd, UUID.randomUUID().toString()));
        }

        @Test
        @DisplayName("should throw when workshop not found")
        void shouldThrowWhenNotFound() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> workshopService.reschedule(WORKSHOP_ID, START_TIME, END_TIME, ROOM_ID));
        }
    }

    // ================================================================
    //  START
    // ================================================================

    @Nested
    @DisplayName("start")
    class StartTests {

        @Test
        @DisplayName("should start published workshop and emit event")
        void shouldStartAndEmitEvent() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(publishedWorkshop));
            when(workshopRepository.save(any(Workshop.class))).thenReturn(publishedWorkshop);

            var result = workshopService.start(WORKSHOP_ID);

            assertNotNull(result);
            assertEquals(WorkshopState.IN_PROGRESS, publishedWorkshop.getState());
            verify(eventPublisher).publishEvent(any(WorkshopEvents.Started.class));
        }

        @Test
        @DisplayName("should throw when workshop not found")
        void shouldThrowWhenNotFound() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> workshopService.start(WORKSHOP_ID));
        }

        @Test
        @DisplayName("should throw when starting from DRAFT")
        void shouldThrowWhenStartingFromDraft() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(draftWorkshop));
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshopService.start(WORKSHOP_ID));
        }
    }

    // ================================================================
    //  COMPLETE
    // ================================================================

    @Nested
    @DisplayName("complete")
    class CompleteTests {

        @Test
        @DisplayName("should complete in-progress workshop and emit event")
        void shouldCompleteAndEmitEvent() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(inProgressWorkshop));
            when(workshopRepository.save(any(Workshop.class))).thenReturn(inProgressWorkshop);

            var result = workshopService.complete(WORKSHOP_ID);

            assertNotNull(result);
            assertEquals(WorkshopState.COMPLETED, inProgressWorkshop.getState());
            verify(eventPublisher).publishEvent(any(WorkshopEvents.Completed.class));
        }

        @Test
        @DisplayName("should throw when workshop not found")
        void shouldThrowWhenNotFound() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> workshopService.complete(WORKSHOP_ID));
        }

        @Test
        @DisplayName("should throw when completing from PUBLISHED")
        void shouldThrowWhenCompletingFromPublished() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(publishedWorkshop));
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshopService.complete(WORKSHOP_ID));
        }
    }

    // ================================================================
    //  CANCEL
    // ================================================================

    @Nested
    @DisplayName("cancel")
    class CancelTests {

        @Test
        @DisplayName("should cancel published workshop and emit event")
        void shouldCancelAndEmitEvent() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(publishedWorkshop));
            when(workshopRepository.save(any(Workshop.class))).thenReturn(publishedWorkshop);

            var result = workshopService.cancel(WORKSHOP_ID);

            assertNotNull(result);
            assertEquals(WorkshopState.CANCELLED, publishedWorkshop.getState());
            verify(eventPublisher).publishEvent(any(WorkshopEvents.Cancelled.class));
        }

        @Test
        @DisplayName("should cancel draft workshop")
        void shouldCancelDraft() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(draftWorkshop));
            when(workshopRepository.save(any(Workshop.class))).thenReturn(draftWorkshop);

            workshopService.cancel(WORKSHOP_ID);

            assertEquals(WorkshopState.CANCELLED, draftWorkshop.getState());
            verify(eventPublisher).publishEvent(any(WorkshopEvents.Cancelled.class));
        }

        @Test
        @DisplayName("should throw when workshop not found")
        void shouldThrowWhenNotFound() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> workshopService.cancel(WORKSHOP_ID));
        }

        @Test
        @DisplayName("should throw when cancelling from COMPLETED")
        void shouldThrowWhenCancellingCompleted() {
            var completed = Workshop.createDraft("Title", "Desc");
            completed.schedule(ROOM_UUID, "Room A", START_TIME, END_TIME, CAPACITY);
            completed.publish(completed.getRoomId(), completed.getRoomDisplayNameSnapshot(),
                    completed.getStartTime(), completed.getEndTime(), completed.getCapacity());
            completed.start();
            completed.complete();

            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(completed));
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshopService.cancel(WORKSHOP_ID));
        }
    }

    // ================================================================
    //  FIND BY ID
    // ================================================================

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("should return workshop when found")
        void shouldReturnWorkshopWhenFound() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.of(publishedWorkshop));

            var result = workshopService.findById(WORKSHOP_ID);

            assertNotNull(result);
            assertEquals(WORKSHOP_UUID, result.id());
            verify(workshopRepository).findById(WORKSHOP_UUID);
        }

        @Test
        @DisplayName("should throw when workshop not found")
        void shouldThrowWhenNotFound() {
            when(workshopRepository.findById(WORKSHOP_UUID)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> workshopService.findById(WORKSHOP_ID));
        }
    }

    // ================================================================
    //  FIND ALL
    // ================================================================

    @Nested
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("should return paginated workshops")
        void shouldReturnPaginatedWorkshops() {
            var pageable = PageRequest.of(0, 20);
            var workshopPage = new PageImpl<Workshop>(List.of(publishedWorkshop), pageable, 1);
            when(workshopRepository.findAll(any(Pageable.class))).thenReturn(workshopPage);

            Page<WorkshopResponse> result = workshopService.findAll(pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(workshopRepository).findAll(pageable);
        }

        @Test
        @DisplayName("should return empty page when no workshops")
        void shouldReturnEmptyPage() {
            var pageable = PageRequest.of(0, 20);
            var emptyPage = new PageImpl<Workshop>(List.of(), pageable, 0);
            when(workshopRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            Page<WorkshopResponse> result = workshopService.findAll(pageable);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
        }
    }
}