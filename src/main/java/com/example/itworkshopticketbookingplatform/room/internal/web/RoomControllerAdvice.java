package com.example.itworkshopticketbookingplatform.room.internal.web;

import org.springframework.http.HttpStatus;
import com.example.itworkshopticketbookingplatform.room.RoomNotFoundException;
import com.example.itworkshopticketbookingplatform.room.internal.exception.DuplicateRoomCodeException;
import com.example.itworkshopticketbookingplatform.room.internal.exception.RoomDomainException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class RoomControllerAdvice {

    @ExceptionHandler(RoomNotFoundException.class)
    ResponseEntity<String> handleRoomNotFoundException(RoomNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateRoomCodeException.class)
    ResponseEntity<String> handleDuplicateRoomCodeException(DuplicateRoomCodeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RoomDomainException.class)
    ResponseEntity<String> handleRoomDomainException(RoomDomainException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}