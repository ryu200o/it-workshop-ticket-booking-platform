package com.example.itworkshopticketbookingplatform.workshop;

import java.util.UUID;

/**
 * Exception thrown when a workshop is not found.
 */
public class WorkshopNotFoundException extends RuntimeException {
    public WorkshopNotFoundException(String workshopId) {
        super("Workshop not found with ID: " + workshopId);
    }

    public WorkshopNotFoundException(UUID workshopId) {
        super("Workshop not found with ID: " + workshopId);
    }
}
