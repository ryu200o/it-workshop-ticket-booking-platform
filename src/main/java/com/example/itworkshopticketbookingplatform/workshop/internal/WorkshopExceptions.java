package com.example.itworkshopticketbookingplatform.workshop.internal;

final class WorkshopExceptions {
    private WorkshopExceptions() {} // Prevent instantiation

    static class InvalidWorkshopStateException extends IllegalStateException {
        InvalidWorkshopStateException(String message) {
            super(message);
        }
    }
}
