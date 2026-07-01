package com.example.itworkshopticketbookingplatform.registration.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface RegistrationRepository extends JpaRepository<Registration, UUID> {
    List<Registration> findByWorkshopId(UUID workshopId);
    List<Registration> findByUserId(UUID userId);
    Optional<Registration> findByWorkshopIdAndUserId(UUID workshopId, UUID userId);
    long countByWorkshopIdAndStatus(UUID workshopId, Registration.Status status);
    List<Registration> findByWorkshopIdAndStatus(UUID workshopId, Registration.Status status);
}
