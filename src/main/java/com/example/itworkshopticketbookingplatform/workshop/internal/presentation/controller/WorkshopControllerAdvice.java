package com.example.itworkshopticketbookingplatform.workshop.internal.presentation.controller;

import com.example.itworkshopticketbookingplatform.workshop.internal.domain.exception.InvalidWorkshopStateException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class WorkshopControllerAdvice {

    @ExceptionHandler(InvalidWorkshopStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidWorkshopStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("INVALID_STATE_TRANSITION", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_ARGUMENT", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("INVALID_STATE", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse("VALIDATION_FAILED", "Validation failed", Instant.now(), errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse("VALIDATION_FAILED", "Validation failed", Instant.now(), errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred", Instant.now()));
    }

    public record ErrorResponse(
            String code,
            String message,
            Instant timestamp
    ) {}

    public record ValidationErrorResponse(
            String code,
            String message,
            Instant timestamp,
            Map<String, String> errors
    ) {}
}