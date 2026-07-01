package com.example.itworkshopticketbookingplatform.registration.internal;

import com.example.itworkshopticketbookingplatform.registration.RegistrationEvents;
import com.example.itworkshopticketbookingplatform.registration.RegistrationNotFoundException;
import com.example.itworkshopticketbookingplatform.registration.dto.RegistrationResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final ApplicationEventPublisher eventPublisher;

    RegistrationServiceImpl(RegistrationRepository registrationRepository,
                            ApplicationEventPublisher eventPublisher) {
        this.registrationRepository = registrationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RegistrationResponse register(UUID workshopId, UUID userId) {
        // Check for duplicate registration
        if (registrationRepository.findByWorkshopIdAndUserId(workshopId, userId).isPresent()) {
            throw new RegistrationExceptions.DuplicateRegistrationException(workshopId, userId);
        }

        // TODO: Validate workshop exists and is in PUBLISHED state via WorkshopExposeAPI
        // TODO: Validate capacity not exceeded via WorkshopExposeAPI

        Registration registration = new Registration(UUID.randomUUID(), workshopId, userId, Instant.now());
        Registration saved = registrationRepository.save(registration);

        eventPublisher.publishEvent(new RegistrationEvents.Registered(
                saved.getId(), saved.getWorkshopId(), saved.getUserId(), Instant.now()
        ));

        return toResponse(saved);
    }

    @Override
    public RegistrationResponse cancel(UUID registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationId));

        registration.cancel();
        Registration saved = registrationRepository.save(registration);

        eventPublisher.publishEvent(new RegistrationEvents.Cancelled(
                saved.getId(), saved.getWorkshopId(), saved.getUserId(),
                "Cancelled by user", Instant.now()
        ));

        return toResponse(saved);
    }

    @Override
    public RegistrationResponse checkIn(UUID registrationId, UUID checkedInBy) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationId));

        registration.checkIn(checkedInBy);
        Registration saved = registrationRepository.save(registration);

        eventPublisher.publishEvent(new RegistrationEvents.Attended(
                saved.getId(), saved.getWorkshopId(), saved.getUserId(),
                checkedInBy, Instant.now()
        ));

        return toResponse(saved);
    }

    @Override
    public RegistrationResponse markNoShow(UUID registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationId));

        registration.markNoShow();
        Registration saved = registrationRepository.save(registration);

        eventPublisher.publishEvent(new RegistrationEvents.NoShow(
                saved.getId(), saved.getWorkshopId(), saved.getUserId(), Instant.now()
        ));

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> findByWorkshop(UUID workshopId) {
        return registrationRepository.findByWorkshopId(workshopId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> findByUser(UUID userId) {
        return registrationRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getAttendance(UUID workshopId) {
        return registrationRepository.countByWorkshopIdAndStatus(workshopId, Registration.Status.ATTENDED);
    }

    private RegistrationResponse toResponse(Registration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getWorkshopId(),
                registration.getUserId(),
                registration.getStatus().name(),
                registration.getRegistrationTime(),
                registration.isCheckedIn(),
                registration.getCheckedInAt(),
                registration.getCheckedInBy(),
                registration.getCreatedAt(),
                registration.getUpdatedAt()
        );
    }
}
