package com.example.itworkshopticketbookingplatform.workshop.internal;

import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.itworkshopticketbookingplatform.workshop.dto.WorkshopRequest;
import com.example.itworkshopticketbookingplatform.workshop.dto.WorkshopResponse;

interface WorkshopService {
    WorkshopResponse createDraft(WorkshopRequest request);
    WorkshopResponse updateContent(String workshopId, WorkshopRequest request);
    WorkshopResponse schedule(String workshopId, Instant startTime, Instant endTime, int capacity, String roomId);
    WorkshopResponse publish(String workshopId);
    WorkshopResponse reschedule(String workshopId, Instant startTime, Instant endTime, String roomId);
    WorkshopResponse start(String workshopId);
    WorkshopResponse complete(String workshopId);
    WorkshopResponse cancel(String workshopId);
    WorkshopResponse findById(String workshopId);
    Page<WorkshopResponse> findAll(Pageable pageable);
}