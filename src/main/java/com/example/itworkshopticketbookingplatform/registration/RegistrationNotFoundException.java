package com.example.itworkshopticketbookingplatform.registration;

import java.util.UUID;

public class RegistrationNotFoundException extends RuntimeException {
    public RegistrationNotFoundException(UUID id) {
        super("Registration not found with ID: " + id);
    }
}
