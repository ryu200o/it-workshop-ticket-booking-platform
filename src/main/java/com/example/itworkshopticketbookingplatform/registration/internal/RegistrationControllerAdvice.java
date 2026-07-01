package com.example.itworkshopticketbookingplatform.registration.internal;

import com.example.itworkshopticketbookingplatform.registration.RegistrationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class RegistrationControllerAdvice {

    @ExceptionHandler(RegistrationNotFoundException.class)
    ResponseEntity<String> handleRegistrationNotFound(RegistrationNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RegistrationExceptions.DuplicateRegistrationException.class)
    ResponseEntity<String> handleDuplicateRegistration(RegistrationExceptions.DuplicateRegistrationException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RegistrationExceptions.InvalidRegistrationStateException.class)
    ResponseEntity<String> handleInvalidState(RegistrationExceptions.InvalidRegistrationStateException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RegistrationExceptions.CapacityExceededException.class)
    ResponseEntity<String> handleCapacityExceeded(RegistrationExceptions.CapacityExceededException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
