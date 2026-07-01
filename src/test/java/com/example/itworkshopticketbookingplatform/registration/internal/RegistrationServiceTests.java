package com.example.itworkshopticketbookingplatform.registration.internal;

import com.example.itworkshopticketbookingplatform.registration.RegistrationEvents;
import com.example.itworkshopticketbookingplatform.registration.RegistrationNotFoundException;
import com.example.itworkshopticketbookingplatform.registration.dto.RegistrationResponse;
import com.example.itworkshopticketbookingplatform.registration.internal.RegistrationExceptions.DuplicateRegistrationException;
import com.example.itworkshopticketbookingplatform.registration.internal.RegistrationExceptions.InvalidRegistrationStateException;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrationServiceImpl Tests")
class RegistrationServiceTests {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Captor
    private ArgumentCaptor<Registration> registrationCaptor;

    static final UUID WORKSHOP_ID = UUID.randomUUID();
    static final UUID USER_ID = UUID.randomUUID();
    static final UUID CHECKED_IN_BY = UUID.randomUUID();
    static final UUID REGISTRATION_ID = UUID.randomUUID();

    Registration confirmedRegistration;
    Registration cancelledRegistration;
    Registration attendedRegistration;
    Registration noShowRegistration;

    @BeforeEach
    void setUp() {
        confirmedRegistration = new Registration(REGISTRATION_ID, WORKSHOP_ID, USER_ID, Instant.now());

        cancelledRegistration = new Registration(REGISTRATION_ID, WORKSHOP_ID, USER_ID,
                Registration.Status.CANCELLED, Instant.now(), false, null, null, Instant.now(), Instant.now());

        attendedRegistration = new Registration(REGISTRATION_ID, WORKSHOP_ID, USER_ID,
                Registration.Status.ATTENDED, Instant.now(), true, Instant.now(), CHECKED_IN_BY, Instant.now(), Instant.now());

        noShowRegistration = new Registration(REGISTRATION_ID, WORKSHOP_ID, USER_ID,
                Registration.Status.NO_SHOW, Instant.now(), false, null, null, Instant.now(), Instant.now());
    }

    // ================================================================
    //  REGISTER
    // ================================================================

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        @DisplayName("should register successfully (CONFIRMED status, emits Registered event)")
        void shouldRegisterSuccessfully() {
            when(registrationRepository.findByWorkshopIdAndUserId(WORKSHOP_ID, USER_ID)).thenReturn(Optional.empty());
            when(registrationRepository.save(any(Registration.class))).thenReturn(confirmedRegistration);

            RegistrationResponse result = registrationService.register(WORKSHOP_ID, USER_ID);

            assertNotNull(result);
            assertEquals(REGISTRATION_ID, result.id());
            assertEquals("CONFIRMED", result.status());
            verify(registrationRepository).save(any(Registration.class));
            verify(eventPublisher).publishEvent(any(RegistrationEvents.Registered.class));
        }

        @Test
        @DisplayName("should throw DuplicateRegistrationException when duplicate")
        void shouldThrowWhenDuplicate() {
            when(registrationRepository.findByWorkshopIdAndUserId(WORKSHOP_ID, USER_ID)).thenReturn(Optional.of(confirmedRegistration));

            assertThrows(DuplicateRegistrationException.class,
                    () -> registrationService.register(WORKSHOP_ID, USER_ID));
            verify(registrationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    // ================================================================
    //  CANCEL
    // ================================================================

    @Nested
    @DisplayName("cancel")
    class CancelTests {

        @Test
        @DisplayName("should cancel CONFIRMED registration (CANCELLED, emits Cancelled)")
        void shouldCancelConfirmed() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(confirmedRegistration));
            when(registrationRepository.save(any(Registration.class))).thenReturn(cancelledRegistration);

            RegistrationResponse result = registrationService.cancel(REGISTRATION_ID);

            assertNotNull(result);
            assertEquals("CANCELLED", result.status());
            verify(registrationRepository).save(any(Registration.class));
            verify(eventPublisher).publishEvent(any(RegistrationEvents.Cancelled.class));
        }

        @Test
        @DisplayName("should throw RegistrationNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.empty());

            assertThrows(RegistrationNotFoundException.class,
                    () -> registrationService.cancel(REGISTRATION_ID));
            verify(registrationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("should throw InvalidRegistrationStateException when already CANCELLED")
        void shouldThrowWhenAlreadyCancelled() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(cancelledRegistration));

            assertThrows(InvalidRegistrationStateException.class,
                    () -> registrationService.cancel(REGISTRATION_ID));
            verify(registrationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("should throw InvalidRegistrationStateException when ATTENDED")
        void shouldThrowWhenAttended() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(attendedRegistration));

            assertThrows(InvalidRegistrationStateException.class,
                    () -> registrationService.cancel(REGISTRATION_ID));
            verify(registrationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("should throw InvalidRegistrationStateException when NO_SHOW")
        void shouldThrowWhenNoShow() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(noShowRegistration));

            assertThrows(InvalidRegistrationStateException.class,
                    () -> registrationService.cancel(REGISTRATION_ID));
            verify(registrationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    // ================================================================
    //  CHECK IN
    // ================================================================

    @Nested
    @DisplayName("checkIn")
    class CheckInTests {

        @Test
        @DisplayName("should check-in CONFIRMED (ATTENDED, emits Attended)")
        void shouldCheckInConfirmed() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(confirmedRegistration));
            when(registrationRepository.save(any(Registration.class))).thenReturn(attendedRegistration);

            RegistrationResponse result = registrationService.checkIn(REGISTRATION_ID, CHECKED_IN_BY);

            assertNotNull(result);
            assertEquals("ATTENDED", result.status());
            assertTrue(result.checkedIn());
            verify(registrationRepository).save(any(Registration.class));
            verify(eventPublisher).publishEvent(any(RegistrationEvents.Attended.class));
        }

        @Test
        @DisplayName("should throw RegistrationNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.empty());

            assertThrows(RegistrationNotFoundException.class,
                    () -> registrationService.checkIn(REGISTRATION_ID, CHECKED_IN_BY));
            verify(registrationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("should throw InvalidRegistrationStateException when CANCELLED")
        void shouldThrowWhenCancelled() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(cancelledRegistration));

            assertThrows(InvalidRegistrationStateException.class,
                    () -> registrationService.checkIn(REGISTRATION_ID, CHECKED_IN_BY));
            verify(registrationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("should throw InvalidRegistrationStateException when ATTENDED")
        void shouldThrowWhenAttended() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(attendedRegistration));

            assertThrows(InvalidRegistrationStateException.class,
                    () -> registrationService.checkIn(REGISTRATION_ID, CHECKED_IN_BY));
            verify(registrationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("should throw InvalidRegistrationStateException when NO_SHOW")
        void shouldThrowWhenNoShow() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(noShowRegistration));

            assertThrows(InvalidRegistrationStateException.class,
                    () -> registrationService.checkIn(REGISTRATION_ID, CHECKED_IN_BY));
            verify(registrationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    // ================================================================
    //  MARK NO SHOW
    // ================================================================

    @Nested
    @DisplayName("markNoShow")
    class MarkNoShowTests {

        @Test
        @DisplayName("should mark CONFIRMED as NO_SHOW (NO_SHOW, emits NoShow)")
        void shouldMarkNoShowConfirmed() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(confirmedRegistration));
            when(registrationRepository.save(any(Registration.class))).thenReturn(noShowRegistration);

            RegistrationResponse result = registrationService.markNoShow(REGISTRATION_ID);

            assertNotNull(result);
            assertEquals("NO_SHOW", result.status());
            verify(registrationRepository).save(any(Registration.class));
            verify(eventPublisher).publishEvent(any(RegistrationEvents.NoShow.class));
        }

        @Test
        @DisplayName("should throw RegistrationNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.empty());

            assertThrows(RegistrationNotFoundException.class,
                    () -> registrationService.markNoShow(REGISTRATION_ID));
            verify(registrationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("should throw InvalidRegistrationStateException when CANCELLED")
        void shouldThrowWhenCancelled() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(cancelledRegistration));

            assertThrows(InvalidRegistrationStateException.class,
                    () -> registrationService.markNoShow(REGISTRATION_ID));
            verify(registrationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("should throw InvalidRegistrationStateException when ATTENDED")
        void shouldThrowWhenAttended() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(attendedRegistration));

            assertThrows(InvalidRegistrationStateException.class,
                    () -> registrationService.markNoShow(REGISTRATION_ID));
            verify(registrationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("should throw InvalidRegistrationStateException when NO_SHOW")
        void shouldThrowWhenNoShow() {
            when(registrationRepository.findById(REGISTRATION_ID)).thenReturn(Optional.of(noShowRegistration));

            assertThrows(InvalidRegistrationStateException.class,
                    () -> registrationService.markNoShow(REGISTRATION_ID));
            verify(registrationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    // ================================================================
    //  FIND BY WORKSHOP
    // ================================================================

    @Nested
    @DisplayName("findByWorkshop")
    class FindByWorkshopTests {

        @Test
        @DisplayName("should return list of registrations for workshop")
        void shouldReturnListForWorkshop() {
            when(registrationRepository.findByWorkshopId(WORKSHOP_ID)).thenReturn(List.of(confirmedRegistration, cancelledRegistration));

            List<RegistrationResponse> results = registrationService.findByWorkshop(WORKSHOP_ID);

            assertEquals(2, results.size());
            verify(registrationRepository).findByWorkshopId(WORKSHOP_ID);
        }

        @Test
        @DisplayName("should return empty list when none found")
        void shouldReturnEmptyList() {
            when(registrationRepository.findByWorkshopId(WORKSHOP_ID)).thenReturn(List.of());

            List<RegistrationResponse> results = registrationService.findByWorkshop(WORKSHOP_ID);

            assertTrue(results.isEmpty());
            verify(registrationRepository).findByWorkshopId(WORKSHOP_ID);
        }
    }

    // ================================================================
    //  FIND BY USER
    // ================================================================

    @Nested
    @DisplayName("findByUser")
    class FindByUserTests {

        @Test
        @DisplayName("should return list of registrations for user")
        void shouldReturnListForUser() {
            when(registrationRepository.findByUserId(USER_ID)).thenReturn(List.of(confirmedRegistration));

            List<RegistrationResponse> results = registrationService.findByUser(USER_ID);

            assertEquals(1, results.size());
            verify(registrationRepository).findByUserId(USER_ID);
        }

        @Test
        @DisplayName("should return empty list when none found")
        void shouldReturnEmptyList() {
            when(registrationRepository.findByUserId(USER_ID)).thenReturn(List.of());

            List<RegistrationResponse> results = registrationService.findByUser(USER_ID);

            assertTrue(results.isEmpty());
            verify(registrationRepository).findByUserId(USER_ID);
        }
    }

    // ================================================================
    //  GET ATTENDANCE
    // ================================================================

    @Nested
    @DisplayName("getAttendance")
    class GetAttendanceTests {

        @Test
        @DisplayName("should return count of ATTENDED registrations")
        void shouldReturnAttendedCount() {
            when(registrationRepository.countByWorkshopIdAndStatus(WORKSHOP_ID, Registration.Status.ATTENDED)).thenReturn(5L);

            long count = registrationService.getAttendance(WORKSHOP_ID);

            assertEquals(5L, count);
            verify(registrationRepository).countByWorkshopIdAndStatus(WORKSHOP_ID, Registration.Status.ATTENDED);
        }
    }
}
