package com.example.itworkshopticketbookingplatform.room.presentation;

import com.example.itworkshopticketbookingplatform.room.domain.DuplicateRoomCodeException;
import com.example.itworkshopticketbookingplatform.room.domain.RoomNotFoundException;
import com.example.itworkshopticketbookingplatform.room.domain.RoomDomainException;
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
