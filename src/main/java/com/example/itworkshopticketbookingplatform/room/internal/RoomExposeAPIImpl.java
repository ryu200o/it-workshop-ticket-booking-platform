package com.example.itworkshopticketbookingplatform.room.internal;

import com.example.itworkshopticketbookingplatform.room.RoomExposeAPI;
import org.springframework.stereotype.Service;

@Service
class RoomExposeAPIImpl implements RoomExposeAPI {

    private final RoomService roomService;

    RoomExposeAPIImpl(RoomService roomService) {
        this.roomService = roomService;
    }
}
