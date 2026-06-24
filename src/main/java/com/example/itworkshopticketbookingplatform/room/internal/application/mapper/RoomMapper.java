package com.example.itworkshopticketbookingplatform.room.internal.application.mapper;

import com.example.itworkshopticketbookingplatform.room.RoomResponse;
import com.example.itworkshopticketbookingplatform.room.internal.domain.model.Room;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoomMapper {

    public RoomResponse toRoomResponse(Room room) {
        return new RoomResponse(room.getId(), room.getRoomCode(), room.getPhysicalCapacity(),
                                room.getLocation(), room.isActive(), room.getCreatedAt(), room.getUpdatedAt());
    }

    public List<RoomResponse> toRoomResponseList(List<Room> rooms) {
        return rooms.stream()
                    .map(this::toRoomResponse)
                    .collect(Collectors.toList());
    }
}
