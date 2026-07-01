package com.example.itworkshopticketbookingplatform.registration.internal;

import com.example.itworkshopticketbookingplatform.registration.dto.RegistrationResponse;
import java.util.List;
import java.util.UUID;

interface RegistrationService {
    RegistrationResponse register(UUID workshopId, UUID userId);
    RegistrationResponse cancel(UUID registrationId);
    RegistrationResponse checkIn(UUID registrationId, UUID checkedInBy);
    RegistrationResponse markNoShow(UUID registrationId);
    List<RegistrationResponse> findByWorkshop(UUID workshopId);
    List<RegistrationResponse> findByUser(UUID userId);
    long getAttendance(UUID workshopId);
}
