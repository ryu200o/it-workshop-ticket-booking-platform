package com.example.itworkshopticketbookingplatform.registration.internal;

import java.util.UUID;

final class RegistrationExceptions {
    private RegistrationExceptions() {}

    static class DuplicateRegistrationException extends RuntimeException {
        DuplicateRegistrationException(UUID workshopId, UUID userId) {
            super("User " + userId + " is already registered for workshop " + workshopId);
        }
    }

    static class InvalidRegistrationStateException extends RuntimeException {
        InvalidRegistrationStateException(String message) {
            super(message);
        }
    }

    static class CapacityExceededException extends RuntimeException {
        CapacityExceededException(UUID workshopId) {
            super("Workshop " + workshopId + " has reached maximum capacity");
        }
    }
}
