package com.example.itworkshopticketbookingplatform.workshop.internal;

import com.example.itworkshopticketbookingplatform.workshop.dto.WorkshopRequest;
import com.example.itworkshopticketbookingplatform.workshop.dto.WorkshopResponse;
import com.example.itworkshopticketbookingplatform.workshop.WorkshopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(WorkshopController.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
@DisplayName("WorkshopController Tests")
class WorkshopControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkshopService workshopService;

    private static final String WORKSHOP_ID = UUID.randomUUID().toString();
    private static final Instant START_TIME = Instant.parse("2027-06-15T10:00:00Z");
    private static final Instant END_TIME = Instant.parse("2027-06-15T12:00:00Z");
    private static final String ROOM_ID = UUID.randomUUID().toString();
    private static final int CAPACITY = 50;

    private WorkshopResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new WorkshopResponse(
                UUID.fromString(WORKSHOP_ID),
                "Workshop Title",
                "Workshop Description",
                UUID.fromString(ROOM_ID),
                "Room A",
                START_TIME,
                END_TIME,
                CAPACITY,
                "DRAFT",
                Instant.now(),
                Instant.now()
        );
    }

    @Nested
    @DisplayName("POST /api/workshops")
    class CreateDraftTests {

        @Test
        @DisplayName("should return 200 when creating draft")
        void shouldCreateDraft() throws Exception {
            var request = new WorkshopRequest("New Workshop", "New description");
            when(workshopService.createDraft(any(WorkshopRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/workshops")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(WORKSHOP_ID))
                    .andExpect(jsonPath("$.title").value("Workshop Title"))
                    .andExpect(jsonPath("$.state").value("DRAFT"))
                    .andDo(document("workshop-create-draft",
                            requestFields(
                                    fieldWithPath("title").description("The title of the workshop"),
                                    fieldWithPath("description").description("The description of the workshop")
                            ),
                            responseFields(
                                    fieldWithPath("id").description("The unique identifier of the workshop"),
                                    fieldWithPath("title").description("The title of the workshop"),
                                    fieldWithPath("description").description("The description of the workshop"),
                                    fieldWithPath("roomId").description("The room ID assigned to the workshop"),
                                    fieldWithPath("roomDisplayNameSnapshot").description("The room display name snapshot"),
                                    fieldWithPath("startTime").description("The start time of the workshop"),
                                    fieldWithPath("endTime").description("The end time of the workshop"),
                                    fieldWithPath("capacity").description("The capacity of the workshop"),
                                    fieldWithPath("state").description("The current state of the workshop"),
                                    fieldWithPath("createdAt").description("The creation timestamp"),
                                    fieldWithPath("updatedAt").description("The last update timestamp")
                            )
                    ));
        }

        @Test
        @DisplayName("should return 400 when title is blank")
        void shouldReturn400WhenTitleBlank() throws Exception {
            var request = new WorkshopRequest("", "Description");

            mockMvc.perform(post("/api/workshops")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when title exceeds 200 chars")
        void shouldReturn400WhenTitleTooLong() throws Exception {
            var request = new WorkshopRequest("A".repeat(201), "Description");

            mockMvc.perform(post("/api/workshops")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/workshops/{id}")
    class UpdateContentTests {

        @Test
        @DisplayName("should return 200 when updating content")
        void shouldUpdateContent() throws Exception {
            var request = new WorkshopRequest("Updated Title", "Updated description");
            when(workshopService.updateContent(eq(WORKSHOP_ID), any(WorkshopRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/workshops/{id}", WORKSHOP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(WORKSHOP_ID))
                    .andDo(document("workshop-update-content",
                            pathParameters(
                                    parameterWithName("id").description("The ID of the workshop to update")
                            ),
                            requestFields(
                                    fieldWithPath("title").description("The updated title"),
                                    fieldWithPath("description").description("The updated description")
                            ),
                            responseFields(
                                    fieldWithPath("id").description("The unique identifier of the workshop"),
                                    fieldWithPath("title").description("The title of the workshop"),
                                    fieldWithPath("description").description("The description of the workshop"),
                                    fieldWithPath("roomId").description("The room ID assigned to the workshop"),
                                    fieldWithPath("roomDisplayNameSnapshot").description("The room display name snapshot"),
                                    fieldWithPath("startTime").description("The start time of the workshop"),
                                    fieldWithPath("endTime").description("The end time of the workshop"),
                                    fieldWithPath("capacity").description("The capacity of the workshop"),
                                    fieldWithPath("state").description("The current state of the workshop"),
                                    fieldWithPath("createdAt").description("The creation timestamp"),
                                    fieldWithPath("updatedAt").description("The last update timestamp")
                            )
                    ));
        }

        @Test
        @DisplayName("should return 400 when title is blank")
        void shouldReturn400WhenTitleBlank() throws Exception {
            var request = new WorkshopRequest("", "Description");

            mockMvc.perform(put("/api/workshops/{id}", WORKSHOP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/workshops/{id}/schedule")
    class ScheduleTests {

        @Test
        @DisplayName("should return 200 when scheduling")
        void shouldSchedule() throws Exception {
            when(workshopService.schedule(eq(WORKSHOP_ID), any(Instant.class), any(Instant.class),
                    eq(CAPACITY), eq(ROOM_ID)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(patch("/api/workshops/{id}/schedule", WORKSHOP_ID)
                            .param("startTime", START_TIME.toString())
                            .param("endTime", END_TIME.toString())
                            .param("capacity", String.valueOf(CAPACITY))
                            .param("roomId", ROOM_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(WORKSHOP_ID))
                    .andDo(document("workshop-schedule",
                            pathParameters(
                                    parameterWithName("id").description("The ID of the workshop to schedule")
                            ),
                            responseFields(
                                    fieldWithPath("id").description("The unique identifier of the workshop"),
                                    fieldWithPath("title").description("The title of the workshop"),
                                    fieldWithPath("description").description("The description of the workshop"),
                                    fieldWithPath("roomId").description("The room ID assigned to the workshop"),
                                    fieldWithPath("roomDisplayNameSnapshot").description("The room display name snapshot"),
                                    fieldWithPath("startTime").description("The start time of the workshop"),
                                    fieldWithPath("endTime").description("The end time of the workshop"),
                                    fieldWithPath("capacity").description("The capacity of the workshop"),
                                    fieldWithPath("state").description("The current state of the workshop"),
                                    fieldWithPath("createdAt").description("The creation timestamp"),
                                    fieldWithPath("updatedAt").description("The last update timestamp")
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("PATCH /api/workshops/{id}/publish")
    class PublishTests {

        @Test
        @DisplayName("should return 200 when publishing")
        void shouldPublish() throws Exception {
            var publishedResponse = new WorkshopResponse(
                    UUID.fromString(WORKSHOP_ID), "Title", "Description",
                    UUID.fromString(ROOM_ID), "Room A",
                    START_TIME, END_TIME, CAPACITY,
                    "PUBLISHED", Instant.now(), Instant.now());
            when(workshopService.publish(eq(WORKSHOP_ID))).thenReturn(publishedResponse);

            mockMvc.perform(patch("/api/workshops/{id}/publish", WORKSHOP_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("PUBLISHED"))
                    .andDo(document("workshop-publish",
                            pathParameters(
                                    parameterWithName("id").description("The ID of the workshop to publish")
                            ),
                            responseFields(
                                    fieldWithPath("id").description("The unique identifier of the workshop"),
                                    fieldWithPath("title").description("The title of the workshop"),
                                    fieldWithPath("description").description("The description of the workshop"),
                                    fieldWithPath("roomId").description("The room ID assigned to the workshop"),
                                    fieldWithPath("roomDisplayNameSnapshot").description("The room display name snapshot"),
                                    fieldWithPath("startTime").description("The start time of the workshop"),
                                    fieldWithPath("endTime").description("The end time of the workshop"),
                                    fieldWithPath("capacity").description("The capacity of the workshop"),
                                    fieldWithPath("state").description("The current state of the workshop"),
                                    fieldWithPath("createdAt").description("The creation timestamp"),
                                    fieldWithPath("updatedAt").description("The last update timestamp")
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("PATCH /api/workshops/{id}/reschedule")
    class RescheduleTests {

        @Test
        @DisplayName("should return 200 when rescheduling")
        void shouldReschedule() throws Exception {
            var rescheduledResponse = new WorkshopResponse(
                    UUID.fromString(WORKSHOP_ID), "Title", "Description",
                    UUID.fromString(ROOM_ID), "Room B",
                    START_TIME, END_TIME, CAPACITY,
                    "PUBLISHED", Instant.now(), Instant.now());
            when(workshopService.reschedule(eq(WORKSHOP_ID), any(Instant.class), any(Instant.class), eq(ROOM_ID)))
                    .thenReturn(rescheduledResponse);

            mockMvc.perform(patch("/api/workshops/{id}/reschedule", WORKSHOP_ID)
                            .param("startTime", START_TIME.toString())
                            .param("endTime", END_TIME.toString())
                            .param("roomId", ROOM_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("PUBLISHED"))
                    .andDo(document("workshop-reschedule",
                            pathParameters(
                                    parameterWithName("id").description("The ID of the workshop to reschedule")
                            ),
                            responseFields(
                                    fieldWithPath("id").description("The unique identifier of the workshop"),
                                    fieldWithPath("title").description("The title of the workshop"),
                                    fieldWithPath("description").description("The description of the workshop"),
                                    fieldWithPath("roomId").description("The room ID assigned to the workshop"),
                                    fieldWithPath("roomDisplayNameSnapshot").description("The room display name snapshot"),
                                    fieldWithPath("startTime").description("The start time of the workshop"),
                                    fieldWithPath("endTime").description("The end time of the workshop"),
                                    fieldWithPath("capacity").description("The capacity of the workshop"),
                                    fieldWithPath("state").description("The current state of the workshop"),
                                    fieldWithPath("createdAt").description("The creation timestamp"),
                                    fieldWithPath("updatedAt").description("The last update timestamp")
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("PATCH /api/workshops/{id}/start")
    class StartTests {

        @Test
        @DisplayName("should return 200 when starting")
        void shouldStart() throws Exception {
            var startedResponse = new WorkshopResponse(
                    UUID.fromString(WORKSHOP_ID), "Title", "Description",
                    UUID.fromString(ROOM_ID), "Room A",
                    START_TIME, END_TIME, CAPACITY,
                    "IN_PROGRESS", Instant.now(), Instant.now());
            when(workshopService.start(eq(WORKSHOP_ID))).thenReturn(startedResponse);

            mockMvc.perform(patch("/api/workshops/{id}/start", WORKSHOP_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("IN_PROGRESS"))
                    .andDo(document("workshop-start",
                            pathParameters(
                                    parameterWithName("id").description("The ID of the workshop to start")
                            ),
                            responseFields(
                                    fieldWithPath("id").description("The unique identifier of the workshop"),
                                    fieldWithPath("title").description("The title of the workshop"),
                                    fieldWithPath("description").description("The description of the workshop"),
                                    fieldWithPath("roomId").description("The room ID assigned to the workshop"),
                                    fieldWithPath("roomDisplayNameSnapshot").description("The room display name snapshot"),
                                    fieldWithPath("startTime").description("The start time of the workshop"),
                                    fieldWithPath("endTime").description("The end time of the workshop"),
                                    fieldWithPath("capacity").description("The capacity of the workshop"),
                                    fieldWithPath("state").description("The current state of the workshop"),
                                    fieldWithPath("createdAt").description("The creation timestamp"),
                                    fieldWithPath("updatedAt").description("The last update timestamp")
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("PATCH /api/workshops/{id}/complete")
    class CompleteTests {

        @Test
        @DisplayName("should return 200 when completing")
        void shouldComplete() throws Exception {
            var completedResponse = new WorkshopResponse(
                    UUID.fromString(WORKSHOP_ID), "Title", "Description",
                    UUID.fromString(ROOM_ID), "Room A",
                    START_TIME, END_TIME, CAPACITY,
                    "COMPLETED", Instant.now(), Instant.now());
            when(workshopService.complete(eq(WORKSHOP_ID))).thenReturn(completedResponse);

            mockMvc.perform(patch("/api/workshops/{id}/complete", WORKSHOP_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("COMPLETED"))
                    .andDo(document("workshop-complete",
                            pathParameters(
                                    parameterWithName("id").description("The ID of the workshop to complete")
                            ),
                            responseFields(
                                    fieldWithPath("id").description("The unique identifier of the workshop"),
                                    fieldWithPath("title").description("The title of the workshop"),
                                    fieldWithPath("description").description("The description of the workshop"),
                                    fieldWithPath("roomId").description("The room ID assigned to the workshop"),
                                    fieldWithPath("roomDisplayNameSnapshot").description("The room display name snapshot"),
                                    fieldWithPath("startTime").description("The start time of the workshop"),
                                    fieldWithPath("endTime").description("The end time of the workshop"),
                                    fieldWithPath("capacity").description("The capacity of the workshop"),
                                    fieldWithPath("state").description("The current state of the workshop"),
                                    fieldWithPath("createdAt").description("The creation timestamp"),
                                    fieldWithPath("updatedAt").description("The last update timestamp")
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("PATCH /api/workshops/{id}/cancel")
    class CancelTests {

        @Test
        @DisplayName("should return 200 when cancelling")
        void shouldCancel() throws Exception {
            var cancelledResponse = new WorkshopResponse(
                    UUID.fromString(WORKSHOP_ID), "Title", "Description",
                    UUID.fromString(ROOM_ID), "Room A",
                    START_TIME, END_TIME, CAPACITY,
                    "CANCELLED", Instant.now(), Instant.now());
            when(workshopService.cancel(eq(WORKSHOP_ID))).thenReturn(cancelledResponse);

            mockMvc.perform(patch("/api/workshops/{id}/cancel", WORKSHOP_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("CANCELLED"))
                    .andDo(document("workshop-cancel",
                            pathParameters(
                                    parameterWithName("id").description("The ID of the workshop to cancel")
                            ),
                            responseFields(
                                    fieldWithPath("id").description("The unique identifier of the workshop"),
                                    fieldWithPath("title").description("The title of the workshop"),
                                    fieldWithPath("description").description("The description of the workshop"),
                                    fieldWithPath("roomId").description("The room ID assigned to the workshop"),
                                    fieldWithPath("roomDisplayNameSnapshot").description("The room display name snapshot"),
                                    fieldWithPath("startTime").description("The start time of the workshop"),
                                    fieldWithPath("endTime").description("The end time of the workshop"),
                                    fieldWithPath("capacity").description("The capacity of the workshop"),
                                    fieldWithPath("state").description("The current state of the workshop"),
                                    fieldWithPath("createdAt").description("The creation timestamp"),
                                    fieldWithPath("updatedAt").description("The last update timestamp")
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("GET /api/workshops/{id}")
    class FindByIdTests {

        @Test
        @DisplayName("should return 200 with workshop details")
        void shouldFindById() throws Exception {
            when(workshopService.findById(WORKSHOP_ID)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/workshops/{id}", WORKSHOP_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(WORKSHOP_ID))
                    .andExpect(jsonPath("$.title").value("Workshop Title"))
                    .andDo(document("workshop-find-by-id",
                            pathParameters(
                                    parameterWithName("id").description("The ID of the workshop to retrieve")
                            ),
                            responseFields(
                                    fieldWithPath("id").description("The unique identifier of the workshop"),
                                    fieldWithPath("title").description("The title of the workshop"),
                                    fieldWithPath("description").description("The description of the workshop"),
                                    fieldWithPath("roomId").description("The room ID assigned to the workshop"),
                                    fieldWithPath("roomDisplayNameSnapshot").description("The room display name snapshot"),
                                    fieldWithPath("startTime").description("The start time of the workshop"),
                                    fieldWithPath("endTime").description("The end time of the workshop"),
                                    fieldWithPath("capacity").description("The capacity of the workshop"),
                                    fieldWithPath("state").description("The current state of the workshop"),
                                    fieldWithPath("createdAt").description("The creation timestamp"),
                                    fieldWithPath("updatedAt").description("The last update timestamp")
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("GET /api/workshops")
    class FindAllTests {

        @Test
        @DisplayName("should return 200 with paginated workshops")
        void shouldFindAll() throws Exception {
            Page<WorkshopResponse> page = new PageImpl<>(List.of(sampleResponse));
            when(workshopService.findAll(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/workshops")
                            .param("page", "0")
                            .param("size", "20")
                            .param("sortBy", "createdAt")
                            .param("sortDirection", "DESC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(WORKSHOP_ID))
                    .andDo(document("workshop-find-all",
                            queryParameters(
                                    parameterWithName("page").description("Page number (0-based)").optional(),
                                    parameterWithName("size").description("Page size").optional(),
                                    parameterWithName("sortBy").description("Sort field").optional(),
                                    parameterWithName("sortDirection").description("Sort direction (ASC/DESC)").optional()
                            ),
                            relaxedResponseFields(
                                    fieldWithPath("content[].id").description("The unique identifier of the workshop"),
                                    fieldWithPath("content[].title").description("The title of the workshop"),
                                    fieldWithPath("content[].description").description("The description of the workshop"),
                                    fieldWithPath("content[].roomId").description("The room ID assigned to the workshop"),
                                    fieldWithPath("content[].roomDisplayNameSnapshot").description("The room display name snapshot"),
                                    fieldWithPath("content[].startTime").description("The start time of the workshop"),
                                    fieldWithPath("content[].endTime").description("The end time of the workshop"),
                                    fieldWithPath("content[].capacity").description("The capacity of the workshop"),
                                    fieldWithPath("content[].state").description("The current state of the workshop"),
                                    fieldWithPath("content[].createdAt").description("The creation timestamp"),
                                    fieldWithPath("content[].updatedAt").description("The last update timestamp")
                            )
                    ));
        }

        @Test
        @DisplayName("should use defaults when no pagination params provided")
        void shouldUseDefaults() throws Exception {
            Page<WorkshopResponse> page = new PageImpl<>(List.of(sampleResponse));
            when(workshopService.findAll(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/workshops")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk());
        }
    }
}
