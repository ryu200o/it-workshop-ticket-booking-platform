package com.example.itworkshopticketbookingplatform.room.internal.web;

import com.example.itworkshopticketbookingplatform.room.RoomService;
import com.example.itworkshopticketbookingplatform.room.dto.RoomActivationRequest;
import com.example.itworkshopticketbookingplatform.room.dto.RoomRequest;
import com.example.itworkshopticketbookingplatform.room.dto.RoomResponse;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(RoomController.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
class RoomControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoomService roomService;

    private RoomResponse sampleRoomResponse;

    @BeforeEach
    void setUp() {
        sampleRoomResponse = new RoomResponse(
                UUID.randomUUID(),
                "ROOM_A",
                10,
                "Building A, Floor 1",
                true, // active
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void createRoom() throws Exception {
        RoomRequest roomRequest = new RoomRequest("ROOM_B", 15, "Building B, Floor 2");

        RoomResponse newRoomResponse = new RoomResponse(
                UUID.randomUUID(),
                roomRequest.roomCode(),
                roomRequest.physicalCapacity(),
                roomRequest.location(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(roomService.createRoom(any(String.class), any(Integer.class), any(String.class)))
                .thenReturn(newRoomResponse);

        this.mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/rooms/" + newRoomResponse.id()))
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

        RoomResponse updatedRoomResponse = new RoomResponse(
                sampleRoomResponse.id(),
                roomRequest.roomCode(),
                roomRequest.physicalCapacity(),
                roomRequest.location(),
                sampleRoomResponse.active(),
                sampleRoomResponse.createdAt(),
                LocalDateTime.now()
        );

        when(roomService.updateRoom(any(UUID.class), any(String.class), any(Integer.class), any(String.class)))
                .thenReturn(updatedRoomResponse);

        this.mockMvc.perform(put("/api/v1/rooms/{roomId}", sampleRoomResponse.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomCode").value("ROOM_A_UPDATED"))
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

        RoomResponse deactivatedRoomResponse = new RoomResponse(
                sampleRoomResponse.id(),
                sampleRoomResponse.roomCode(),
                sampleRoomResponse.physicalCapacity(),
                sampleRoomResponse.location(),
                false,
                sampleRoomResponse.createdAt(),
                LocalDateTime.now()
        );

        when(roomService.activateDeactivateRoom(any(UUID.class), any(Boolean.class)))
                .thenReturn(deactivatedRoomResponse);

        this.mockMvc.perform(patch("/api/v1/rooms/{roomId}/activation", sampleRoomResponse.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andDo(document("activate-deactivate-room",
                        pathParameters(
                                parameterWithName("roomId").description("The ID of the room to activate/deactivate.")
                        ),
                        requestFields(
                                fieldWithPath("active").description("The desired active status for the room (true to activate, false to deactivate).")
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
    void getRoomList() throws Exception {
        List<RoomResponse> roomResponses = Arrays.asList(
                sampleRoomResponse,
                new RoomResponse(UUID.randomUUID(), "ROOM_C", 20, "Building C, Ground Floor", true, LocalDateTime.now(), LocalDateTime.now())
        );
        when(roomService.getRoomList()).thenReturn(roomResponses);

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
                .thenReturn(sampleRoomResponse);

        this.mockMvc.perform(get("/api/v1/rooms/{roomId}", sampleRoomResponse.id())
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
