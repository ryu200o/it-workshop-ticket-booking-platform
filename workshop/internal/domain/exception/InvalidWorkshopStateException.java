package com.example.itworkshopticketbookingplatform.workshop.internal.domain.exception;

/**
 * Exception thrown when an invalid state transition is attempted for a workshop.
 */
public class InvalidWorkshopStateException extends RuntimeException {
    public InvalidWorkshopStateException(String message) {
        super(message);
    }
}