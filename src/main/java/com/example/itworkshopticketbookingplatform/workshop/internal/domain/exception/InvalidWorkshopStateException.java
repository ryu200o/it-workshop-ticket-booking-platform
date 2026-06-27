package com.example.itworkshopticketbookingplatform.workshop.internal.domain.exception;

public class InvalidWorkshopStateException extends IllegalStateException {

    public InvalidWorkshopStateException(String message) {
        super(message);
    }
}