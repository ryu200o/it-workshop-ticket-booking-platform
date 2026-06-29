package com.example.itworkshopticketbookingplatform.room.internal;

import com.example.itworkshopticketbookingplatform.room.RoomNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class RoomControllerAdvice {

    @ExceptionHandler(RoomNotFoundException.class)
    ResponseEntity<String> handleRoomNotFoundException(RoomNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RoomExceptions.DuplicateRoomCodeException.class)
    ResponseEntity<String> handleDuplicateRoomCodeException(RoomExceptions.DuplicateRoomCodeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RoomExceptions.RoomDomainException.class)
    ResponseEntity<String> handleRoomDomainException(RoomExceptions.RoomDomainException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
