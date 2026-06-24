package com.example.itworkshopticketbookingplatform.room;

import com.example.itworkshopticketbookingplatform.room.application.RoomService;
import com.example.itworkshopticketbookingplatform.room.domain.Room;
import com.example.itworkshopticketbookingplatform.room.presentation.RoomActivationRequest;
import com.example.itworkshopticketbookingplatform.room.presentation.RoomController;
import com.example.itworkshopticketbookingplatform.room.presentation.RoomRequest;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(RoomController.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
public class RoomControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoomService roomService;

    private Room sampleRoom;

    @BeforeEach
    void setUp() {
        sampleRoom = new Room(
                UUID.randomUUID(),
                "ROOM_A",
                10,
                "Building A, Floor 1"
        );
    }

    @Test
    void createRoom() throws Exception {
        RoomRequest roomRequest = new RoomRequest("ROOM_B", 15, "Building B, Floor 2");

        Room newRoom = new Room(
                UUID.randomUUID(),
                roomRequest.roomCode(),
                roomRequest.physicalCapacity(),
                roomRequest.location()
        );

        when(roomService.createRoom(any(String.class), any(Integer.class), any(String.class)))
                .thenReturn(newRoom);

        this.mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/rooms/" + newRoom.getId()))
                .andDo(document("create-room",
                        requestFields(
                                fieldWithPath("roomCode").description("The unique code for the room."),
                                fieldWithPath("physicalCapacity").description("The physical capacity of the room."),
                                fieldWithPath("location").description("The location of the room.")
                        ),
                        responseFields(
                                fieldWithPath("id").description("The unique identifier of the room.").type(JsonFieldType.STRING),
                                fieldWithPath("roomCode").description("The unique code for the room.").type(JsonFieldType.STRING),
                                fieldWithPath("physicalCapacity").description("The physical capacity of the room.").type(JsonFieldType.NUMBER),
                                fieldWithPath("location").description("The location of the room.").type(JsonFieldType.STRING),
                                fieldWithPath("active").description("The active status of the room.").type(JsonFieldType.BOOLEAN),
                                fieldWithPath("createdAt").description("The creation timestamp of the room.").type(JsonFieldType.STRING).optional(),
                                fieldWithPath("updatedAt").description("The last update timestamp of the room.").type(JsonFieldType.STRING).optional()
                        )
                ));
    }

    @Test
    void updateRoom() throws Exception {
        RoomRequest roomRequest = new RoomRequest("ROOM_A_UPDATED", 12, "Building A, Floor 1, Wing East");

        Room updatedRoom = new Room(
                sampleRoom.getId(),
                roomRequest.roomCode(),
                roomRequest.physicalCapacity(),
                roomRequest.location()
        );

        when(roomService.updateRoom(any(UUID.class), any(String.class), any(Integer.class), any(String.class)))
                .thenReturn(updatedRoom);

        this.mockMvc.perform(put("/api/v1/rooms/{roomId}", sampleRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequest)))
                .andExpect(status().isOk())
                .andDo(document("update-room",
                        pathParameters(
                                parameterWithName("roomId").description("The ID of the room to update.")
                        ),
                        requestFields(
                                fieldWithPath("roomCode").description("The updated unique code for the room."),
                                fieldWithPath("physicalCapacity").description("The updated physical capacity of the room."),
                                fieldWithPath("location").description("The updated location of the room.")
                        ),
                        responseFields(
                                fieldWithPath("id").description("The unique identifier of the room.").type(JsonFieldType.STRING),
                                fieldWithPath("roomCode").description("The updated unique code for the room.").type(JsonFieldType.STRING),
                                fieldWithPath("physicalCapacity").description("The updated physical capacity of the room.").type(JsonFieldType.NUMBER),
                                fieldWithPath("location").description("The updated location of the room.").type(JsonFieldType.STRING),
                                fieldWithPath("active").description("The active status of the room.").type(JsonFieldType.BOOLEAN),
                                fieldWithPath("createdAt").description("The creation timestamp of the room.").type(JsonFieldType.STRING).optional(),
                                fieldWithPath("updatedAt").description("The last update timestamp of the room.").type(JsonFieldType.STRING).optional()
                        )
                ));
    }

    @Test
    void activateDeactivateRoom() throws Exception {
        RoomActivationRequest activationRequest = new RoomActivationRequest(false);

        Room deactivatedRoom = new Room(
                sampleRoom.getId(),
                sampleRoom.getRoomCode(),
                sampleRoom.getPhysicalCapacity(),
                sampleRoom.getLocation()
        );
        deactivatedRoom.deactivate();

        when(roomService.activateDeactivateRoom(any(UUID.class), any(Boolean.class)))
                .thenReturn(deactivatedRoom);

        this.mockMvc.perform(patch("/api/v1/rooms/{roomId}/activation", sampleRoom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activationRequest)))
                .andExpect(status().isOk())
                .andDo(document("activate-deactivate-room",
                        pathParameters(
                                parameterWithName("roomId").description("The ID of the room to activate/deactivate.")
                        ),
                        requestFields(
                                fieldWithPath("active").description("The desired active status for the room (true to activate, false to deactivate).")
                        )
                ));
    }

    @Test
    void getRoomList() throws Exception {
        List<Room> rooms = Arrays.asList(
                sampleRoom,
                new Room(UUID.randomUUID(), "ROOM_C", 20, "Building C, Ground Floor")
        );
        when(roomService.getRoomList()).thenReturn(rooms);

        this.mockMvc.perform(get("/api/v1/rooms")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-room-list",
                        responseFields(
                                fieldWithPath("[].id").description("The unique identifier of the room.").type(JsonFieldType.STRING),
                                fieldWithPath("[].roomCode").description("The unique code for the room.").type(JsonFieldType.STRING),
                                fieldWithPath("[].physicalCapacity").description("The physical capacity of the room.").type(JsonFieldType.NUMBER),
                                fieldWithPath("[].location").description("The location of the room.").type(JsonFieldType.STRING),
                                fieldWithPath("[].active").description("The active status of the room.").type(JsonFieldType.BOOLEAN),
                                fieldWithPath("[].createdAt").description("The creation timestamp of the room.").type(JsonFieldType.STRING).optional(),
                                fieldWithPath("[].updatedAt").description("The last update timestamp of the room.").type(JsonFieldType.STRING).optional()
                        )
                ));
    }

    @Test
    void getRoomDetail() throws Exception {
        when(roomService.getRoomDetail(any(UUID.class)))
                .thenReturn(sampleRoom);

        this.mockMvc.perform(get("/api/v1/rooms/{roomId}", sampleRoom.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-room-detail",
                        pathParameters(
                                parameterWithName("roomId").description("The ID of the room to retrieve.")
                        ),
                        responseFields(
                                fieldWithPath("id").description("The unique identifier of the room.").type(JsonFieldType.STRING),
                                fieldWithPath("roomCode").description("The unique code for the room.").type(JsonFieldType.STRING),
                                fieldWithPath("physicalCapacity").description("The physical capacity of the room.").type(JsonFieldType.NUMBER),
                                fieldWithPath("location").description("The location of the room.").type(JsonFieldType.STRING),
                                fieldWithPath("active").description("The active status of the room.").type(JsonFieldType.BOOLEAN),
                                fieldWithPath("createdAt").description("The creation timestamp of the room.").type(JsonFieldType.STRING).optional(),
                                fieldWithPath("updatedAt").description("The last update timestamp of the room.").type(JsonFieldType.STRING).optional()
                        )
                ));
    }
}
