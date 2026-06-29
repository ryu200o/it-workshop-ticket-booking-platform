package com.example.itworkshopticketbookingplatform.workshop.internal;

import com.example.itworkshopticketbookingplatform.workshop.internal.WorkshopExceptions.InvalidWorkshopStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Workshop Domain Model Tests")
class WorkshopTest {

    // ============ Helpers ============

    private static final UUID ROOM_ID = UUID.randomUUID();
    private static final String ROOM_NAME = "Room A";
    private static final Instant FUTURE_START = Instant.now().plusSeconds(3600);
    private static final Instant FUTURE_END = Instant.now().plusSeconds(7200);
    private static final int CAPACITY = 50;

    private static Workshop createScheduledPublishedWorkshop() {
        Workshop w = Workshop.createDraft("Title", "Description");
        w.schedule(ROOM_ID, ROOM_NAME, FUTURE_START, FUTURE_END, CAPACITY);
        w.publish(w.getRoomId(), w.getRoomDisplayNameSnapshot(), w.getStartTime(), w.getEndTime(), w.getCapacity());
        return w;
    }

    // ================================================================
    //  CREATE DRAFT
    // ================================================================

    @Nested
    @DisplayName("createDraft")
    class CreateDraftTests {

        @Test
        @DisplayName("should create a workshop in DRAFT state")
        void shouldCreateDraftInDraftState() {
            var workshop = Workshop.createDraft("Workshop Title", "A description");

            assertAll(
                    () -> assertNotNull(workshop.getId()),
                    () -> assertEquals("Workshop Title", workshop.getTitle()),
                    () -> assertEquals("A description", workshop.getDescription()),
                    () -> assertEquals(WorkshopState.DRAFT, workshop.getState()),
                    () -> assertEquals(0, workshop.getCapacity()),
                    () -> assertNull(workshop.getRoomId()),
                    () -> assertNull(workshop.getStartTime()),
                    () -> assertNull(workshop.getEndTime()),
                    () -> assertNotNull(workshop.getCreatedAt()),
                    () -> assertNotNull(workshop.getUpdatedAt())
            );
        }

        @Test
        @DisplayName("should reject blank title")
        void shouldRejectBlankTitle() {
            assertThrows(IllegalArgumentException.class, () -> Workshop.createDraft("", "Description"));
            assertThrows(IllegalArgumentException.class, () -> Workshop.createDraft("   ", "Description"));
        }

        @Test
        @DisplayName("should reject null title")
        void shouldRejectNullTitle() {
            assertThrows(IllegalArgumentException.class, () -> Workshop.createDraft(null, "Description"));
        }

        @Test
        @DisplayName("should reject title exceeding 200 characters")
        void shouldRejectTitleExceeding200Chars() {
            String longTitle = "A".repeat(201);
            assertThrows(IllegalArgumentException.class, () -> Workshop.createDraft(longTitle, "Description"));
        }

        @Test
        @DisplayName("should allow null description")
        void shouldAllowNullDescription() {
            var workshop = Workshop.createDraft("Title", null);
            assertNull(workshop.getDescription());
        }
    }

    // ================================================================
    //  SCHEDULE
    // ================================================================

    @Nested
    @DisplayName("schedule")
    class ScheduleTests {

        @Test
        @DisplayName("should set scheduling info in DRAFT state")
        void shouldScheduleInDraft() {
            var workshop = Workshop.createDraft("Title", "Description");
            workshop.schedule(ROOM_ID, ROOM_NAME, FUTURE_START, FUTURE_END, CAPACITY);

            assertAll(
                    () -> assertEquals(ROOM_ID, workshop.getRoomId()),
                    () -> assertEquals(ROOM_NAME, workshop.getRoomDisplayNameSnapshot()),
                    () -> assertEquals(FUTURE_START, workshop.getStartTime()),
                    () -> assertEquals(FUTURE_END, workshop.getEndTime()),
                    () -> assertEquals(CAPACITY, workshop.getCapacity()),
                    () -> assertEquals(WorkshopState.DRAFT, workshop.getState())
            );
        }

        @Test
        @DisplayName("should reject schedule in PUBLISHED state")
        void shouldRejectScheduleInPublished() {
            var workshop = createScheduledPublishedWorkshop();
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshop.schedule(ROOM_ID, ROOM_NAME, FUTURE_START, FUTURE_END, CAPACITY));
        }

        @Test
        @DisplayName("should reject schedule in IN_PROGRESS state")
        void shouldRejectScheduleInProgress() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.start();
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshop.schedule(ROOM_ID, ROOM_NAME, FUTURE_START, FUTURE_END, CAPACITY));
        }

        @Test
        @DisplayName("should reject null roomId")
        void shouldRejectNullRoomId() {
            var workshop = Workshop.createDraft("Title", "Description");
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.schedule(null, ROOM_NAME, FUTURE_START, FUTURE_END, CAPACITY));
        }

        @Test
        @DisplayName("should reject null startTime")
        void shouldRejectNullStartTime() {
            var workshop = Workshop.createDraft("Title", "Description");
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.schedule(ROOM_ID, ROOM_NAME, null, FUTURE_END, CAPACITY));
        }

        @Test
        @DisplayName("should reject null endTime")
        void shouldRejectNullEndTime() {
            var workshop = Workshop.createDraft("Title", "Description");
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.schedule(ROOM_ID, ROOM_NAME, FUTURE_START, null, CAPACITY));
        }

        @Test
        @DisplayName("should reject capacity of zero")
        void shouldRejectZeroCapacity() {
            var workshop = Workshop.createDraft("Title", "Description");
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.schedule(ROOM_ID, ROOM_NAME, FUTURE_START, FUTURE_END, 0));
        }

        @Test
        @DisplayName("should reject negative capacity")
        void shouldRejectNegativeCapacity() {
            var workshop = Workshop.createDraft("Title", "Description");
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.schedule(ROOM_ID, ROOM_NAME, FUTURE_START, FUTURE_END, -1));
        }

        @Test
        @DisplayName("should reject startTime after endTime")
        void shouldRejectStartTimeAfterEndTime() {
            var workshop = Workshop.createDraft("Title", "Description");
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.schedule(ROOM_ID, ROOM_NAME, FUTURE_END, FUTURE_START, CAPACITY));
        }

        @Test
        @DisplayName("should reject equal startTime and endTime")
        void shouldRejectEqualStartAndEndTime() {
            var workshop = Workshop.createDraft("Title", "Description");
            var sameTime = Instant.now().plusSeconds(3600);
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.schedule(ROOM_ID, ROOM_NAME, sameTime, sameTime, CAPACITY));
        }
    }

    // ================================================================
    //  PUBLISH
    // ================================================================

    @Nested
    @DisplayName("publish")
    class PublishTests {

        @Test
        @DisplayName("should transition from DRAFT to PUBLISHED with valid data")
        void shouldPublishFromDraft() {
            var workshop = createScheduledPublishedWorkshop();

            assertEquals(WorkshopState.PUBLISHED, workshop.getState());
            assertNotNull(workshop.getRoomId());
            assertNotNull(workshop.getStartTime());
            assertNotNull(workshop.getEndTime());
            assertTrue(workshop.getCapacity() > 0);
        }

        @Test
        @DisplayName("should reject publish from PUBLISHED state")
        void shouldRejectPublishFromPublished() {
            var workshop = createScheduledPublishedWorkshop();
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshop.publish(workshop.getRoomId(), workshop.getRoomDisplayNameSnapshot(),
                            workshop.getStartTime(), workshop.getEndTime(), workshop.getCapacity()));
        }

        @Test
        @DisplayName("should reject publish from IN_PROGRESS state")
        void shouldRejectPublishFromInProgress() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.start();
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshop.publish(ROOM_ID, ROOM_NAME, FUTURE_START, FUTURE_END, CAPACITY));
        }

        @Test
        @DisplayName("should reject publish from COMPLETED state")
        void shouldRejectPublishFromCompleted() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.start();
            workshop.complete();
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshop.publish(ROOM_ID, ROOM_NAME, FUTURE_START, FUTURE_END, CAPACITY));
        }

        @Test
        @DisplayName("should reject publish from CANCELLED state")
        void shouldRejectPublishFromCancelled() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.cancel();
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshop.publish(ROOM_ID, ROOM_NAME, FUTURE_START, FUTURE_END, CAPACITY));
        }

        @Test
        @DisplayName("should reject publish with null roomId")
        void shouldRejectPublishWithNullRoomId() {
            var workshop = Workshop.createDraft("Title", "Description");
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.publish(null, ROOM_NAME, FUTURE_START, FUTURE_END, CAPACITY));
        }

        @Test
        @DisplayName("should reject publish with past startTime")
        void shouldRejectPublishWithPastStartTime() {
            var workshop = Workshop.createDraft("Title", "Description");
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.publish(ROOM_ID, ROOM_NAME,
                            Instant.now().minusSeconds(3600), FUTURE_END, CAPACITY));
        }
    }

    // ================================================================
    //  RESCHEDULE
    // ================================================================

    @Nested
    @DisplayName("reschedule")
    class RescheduleTests {

        @Test
        @DisplayName("should be allowed in PUBLISHED state")
        void shouldRescheduleInPublished() {
            var workshop = createScheduledPublishedWorkshop();
            var newStart = Instant.now().plusSeconds(10000);
            var newEnd = Instant.now().plusSeconds(14000);

            workshop.reschedule(newStart, newEnd, workshop.getRoomId(), ROOM_NAME, false);

            assertAll(
                    () -> assertEquals(WorkshopState.PUBLISHED, workshop.getState()),
                    () -> assertEquals(newStart, workshop.getStartTime()),
                    () -> assertEquals(newEnd, workshop.getEndTime())
            );
        }

        @Test
        @DisplayName("should update room when roomChanged is true")
        void shouldUpdateRoomWhenChanged() {
            var workshop = createScheduledPublishedWorkshop();
            var newRoomId = UUID.randomUUID();
            var newRoomName = "Room B";
            var newStart = Instant.now().plusSeconds(10000);
            var newEnd = Instant.now().plusSeconds(14000);

            workshop.reschedule(newStart, newEnd, newRoomId, newRoomName, true);

            assertEquals(newRoomId, workshop.getRoomId());
            assertEquals(newRoomName, workshop.getRoomDisplayNameSnapshot());
        }

        @Test
        @DisplayName("should NOT update room when roomChanged is false")
        void shouldNotUpdateRoomWhenNotChanged() {
            var workshop = createScheduledPublishedWorkshop();
            var originalRoomId = workshop.getRoomId();

            workshop.reschedule(Instant.now().plusSeconds(10000), Instant.now().plusSeconds(14000),
                    UUID.randomUUID(), "Room B", false);

            assertEquals(originalRoomId, workshop.getRoomId());
        }

        @Test
        @DisplayName("should reject reschedule in DRAFT state")
        void shouldRejectRescheduleInDraft() {
            var workshop = Workshop.createDraft("Title", "Description");
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshop.reschedule(FUTURE_START, FUTURE_END, ROOM_ID, ROOM_NAME, false));
        }

        @Test
        @DisplayName("should reject reschedule in IN_PROGRESS state")
        void shouldRejectRescheduleInProgress() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.start();
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshop.reschedule(FUTURE_START, FUTURE_END, ROOM_ID, ROOM_NAME, false));
        }

        @Test
        @DisplayName("should reject null startTime")
        void shouldRejectNullStartTime() {
            var workshop = createScheduledPublishedWorkshop();
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.reschedule(null, FUTURE_END, ROOM_ID, ROOM_NAME, false));
        }

        @Test
        @DisplayName("should reject null endTime")
        void shouldRejectNullEndTime() {
            var workshop = createScheduledPublishedWorkshop();
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.reschedule(FUTURE_START, null, ROOM_ID, ROOM_NAME, false));
        }

        @Test
        @DisplayName("should reject startTime after endTime")
        void shouldRejectStartTimeAfterEndTime() {
            var workshop = createScheduledPublishedWorkshop();
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.reschedule(FUTURE_END, FUTURE_START, ROOM_ID, ROOM_NAME, false));
        }
    }

    // ================================================================
    //  START
    // ================================================================

    @Nested
    @DisplayName("start")
    class StartTests {

        @Test
        @DisplayName("should transition from PUBLISHED to IN_PROGRESS")
        void shouldStartFromPublished() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.start();

            assertEquals(WorkshopState.IN_PROGRESS, workshop.getState());
        }

        @Test
        @DisplayName("should reject start from DRAFT")
        void shouldRejectStartFromDraft() {
            var workshop = Workshop.createDraft("Title", "Description");
            assertThrows(InvalidWorkshopStateException.class, workshop::start);
        }

        @Test
        @DisplayName("should reject start from IN_PROGRESS")
        void shouldRejectStartFromInProgress() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.start();
            assertThrows(InvalidWorkshopStateException.class, workshop::start);
        }

        @Test
        @DisplayName("should reject start from COMPLETED")
        void shouldRejectStartFromCompleted() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.start();
            workshop.complete();
            assertThrows(InvalidWorkshopStateException.class, workshop::start);
        }

        @Test
        @DisplayName("should reject start from CANCELLED")
        void shouldRejectStartFromCancelled() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.cancel();
            assertThrows(InvalidWorkshopStateException.class, workshop::start);
        }
    }

    // ================================================================
    //  COMPLETE
    // ================================================================

    @Nested
    @DisplayName("complete")
    class CompleteTests {

        @Test
        @DisplayName("should transition from IN_PROGRESS to COMPLETED")
        void shouldCompleteFromInProgress() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.start();
            workshop.complete();

            assertEquals(WorkshopState.COMPLETED, workshop.getState());
        }

        @Test
        @DisplayName("should reject complete from DRAFT")
        void shouldRejectCompleteFromDraft() {
            var workshop = Workshop.createDraft("Title", "Description");
            assertThrows(InvalidWorkshopStateException.class, workshop::complete);
        }

        @Test
        @DisplayName("should reject complete from PUBLISHED")
        void shouldRejectCompleteFromPublished() {
            var workshop = createScheduledPublishedWorkshop();
            assertThrows(InvalidWorkshopStateException.class, workshop::complete);
        }

        @Test
        @DisplayName("should reject complete from COMPLETED")
        void shouldRejectCompleteFromCompleted() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.start();
            workshop.complete();
            assertThrows(InvalidWorkshopStateException.class, workshop::complete);
        }

        @Test
        @DisplayName("should reject complete from CANCELLED")
        void shouldRejectCompleteFromCancelled() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.cancel();
            assertThrows(InvalidWorkshopStateException.class, workshop::complete);
        }
    }

    // ================================================================
    //  CANCEL
    // ================================================================

    @Nested
    @DisplayName("cancel")
    class CancelTests {

        @Test
        @DisplayName("should cancel from DRAFT")
        void shouldCancelFromDraft() {
            var workshop = Workshop.createDraft("Title", "Description");
            workshop.cancel();
            assertEquals(WorkshopState.CANCELLED, workshop.getState());
        }

        @Test
        @DisplayName("should cancel from PUBLISHED")
        void shouldCancelFromPublished() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.cancel();
            assertEquals(WorkshopState.CANCELLED, workshop.getState());
        }

        @Test
        @DisplayName("should cancel from IN_PROGRESS")
        void shouldCancelFromInProgress() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.start();
            workshop.cancel();
            assertEquals(WorkshopState.CANCELLED, workshop.getState());
        }

        @Test
        @DisplayName("should reject cancel from COMPLETED")
        void shouldRejectCancelFromCompleted() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.start();
            workshop.complete();
            assertThrows(InvalidWorkshopStateException.class, workshop::cancel);
        }

        @Test
        @DisplayName("should reject cancel from CANCELLED")
        void shouldRejectCancelFromCancelled() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.cancel();
            assertThrows(InvalidWorkshopStateException.class, workshop::cancel);
        }
    }

    // ================================================================
    //  UPDATE CONTENT
    // ================================================================

    @Nested
    @DisplayName("updateContent")
    class UpdateContentTests {

        @Test
        @DisplayName("should update content in DRAFT state")
        void shouldUpdateContentInDraft() {
            var workshop = Workshop.createDraft("Old Title", "Old description");
            workshop.updateContent("New Title", "New description");

            assertAll(
                    () -> assertEquals("New Title", workshop.getTitle()),
                    () -> assertEquals("New description", workshop.getDescription())
            );
        }

        @Test
        @DisplayName("should update content in PUBLISHED state")
        void shouldUpdateContentInPublished() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.updateContent("Updated Title", "Updated description");

            assertAll(
                    () -> assertEquals("Updated Title", workshop.getTitle()),
                    () -> assertEquals("Updated description", workshop.getDescription()),
                    () -> assertEquals(WorkshopState.PUBLISHED, workshop.getState())
            );
        }

        @Test
        @DisplayName("should reject updateContent in IN_PROGRESS state")
        void shouldRejectUpdateInProgress() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.start();
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshop.updateContent("New Title", "New description"));
        }

        @Test
        @DisplayName("should reject updateContent in COMPLETED state")
        void shouldRejectUpdateCompleted() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.start();
            workshop.complete();
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshop.updateContent("New Title", "New description"));
        }

        @Test
        @DisplayName("should reject updateContent in CANCELLED state")
        void shouldRejectUpdateCancelled() {
            var workshop = createScheduledPublishedWorkshop();
            workshop.cancel();
            assertThrows(InvalidWorkshopStateException.class,
                    () -> workshop.updateContent("New Title", "New description"));
        }

        @Test
        @DisplayName("should reject blank title in updateContent")
        void shouldRejectBlankTitle() {
            var workshop = Workshop.createDraft("Title", "Description");
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.updateContent("", "New description"));
        }

        @Test
        @DisplayName("should reject title exceeding 200 chars in updateContent")
        void shouldRejectTitleExceeding200Chars() {
            var workshop = Workshop.createDraft("Title", "Description");
            String longTitle = "A".repeat(201);
            assertThrows(IllegalArgumentException.class,
                    () -> workshop.updateContent(longTitle, "New description"));
        }
    }

    // ================================================================
    //  STATE TRANSITION TABLE - COMPREHENSIVE
    // ================================================================

    @Nested
    @DisplayName("state transition table")
    class StateTransitionTableTests {

        @Test
        @DisplayName("should follow the complete state machine")
        void shouldFollowFullLifecycle() {
            // DRAFT -> PUBLISHED -> IN_PROGRESS -> COMPLETED
            var workshop = createScheduledPublishedWorkshop();
            assertEquals(WorkshopState.PUBLISHED, workshop.getState());
            workshop.start();
            assertEquals(WorkshopState.IN_PROGRESS, workshop.getState());
            workshop.complete();
            assertEquals(WorkshopState.COMPLETED, workshop.getState());
        }

        @Test
        @DisplayName("should allow cancel from DRAFT, PUBLISHED, IN_PROGRESS")
        void shouldAllowCancelFromMultipleStates() {
            var w1 = Workshop.createDraft("T1", "D1");
            w1.cancel();
            assertEquals(WorkshopState.CANCELLED, w1.getState());

            var w2 = createScheduledPublishedWorkshop();
            w2.cancel();
            assertEquals(WorkshopState.CANCELLED, w2.getState());

            var w3 = createScheduledPublishedWorkshop();
            w3.start();
            w3.cancel();
            assertEquals(WorkshopState.CANCELLED, w3.getState());
        }
    }

    // ================================================================
    //  FROM PERSISTENCE
    // ================================================================

    @Nested
    @DisplayName("rehydration from persistence")
    class FromPersistenceTests {

        @Test
        @DisplayName("should rehydrate workshop from persistence data")
        void shouldRehydrateFromPersistence() {
            var id = UUID.randomUUID();
            var now = Instant.now();
            var start = now.plusSeconds(3600);
            var end = now.plusSeconds(7200);

            var workshop = new Workshop(id, "Title", "Description",
                    ROOM_ID, ROOM_NAME, start, end, CAPACITY,
                    WorkshopState.PUBLISHED, now, now);

            assertAll(
                    () -> assertEquals(id, workshop.getId()),
                    () -> assertEquals("Title", workshop.getTitle()),
                    () -> assertEquals(WorkshopState.PUBLISHED, workshop.getState()),
                    () -> assertEquals(ROOM_ID, workshop.getRoomId()),
                    () -> assertEquals(start, workshop.getStartTime()),
                    () -> assertEquals(end, workshop.getEndTime()),
                    () -> assertEquals(CAPACITY, workshop.getCapacity()),
                    () -> assertEquals(now, workshop.getCreatedAt())
            );
        }
    }
}