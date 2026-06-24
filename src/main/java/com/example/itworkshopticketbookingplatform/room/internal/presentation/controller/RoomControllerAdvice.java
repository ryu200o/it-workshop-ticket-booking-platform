package com.example.itworkshopticketbookingplatform.room.internal.presentation.controller;

import com.example.itworkshopticketbookingplatform.room.internal.domain.exception.DuplicateRoomCodeException;
import com.example.itworkshopticketbookingplatform.room.internal.domain.exception.RoomNotFoundException;
import com.example.itworkshopticketbookingplatform.room.internal.domain.exception.RoomDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RoomControllerAdvice {

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<String> handleRoomNotFoundException(RoomNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateRoomCodeException.class)
    public ResponseEntity<String> handleDuplicateRoomCodeException(DuplicateRoomCodeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RoomDomainException.class)
    public ResponseEntity<String> handleRoomDomainException(RoomDomainException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
