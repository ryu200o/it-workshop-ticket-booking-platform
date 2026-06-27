package com.example.itworkshopticketbookingplatform.workshop.internal.application.mapper;

import com.example.itworkshopticketbookingplatform.workshop.WorkshopRequest;
import com.example.itworkshopticketbookingplatform.workshop.WorkshopResponse;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.Workshop;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface WorkshopMapper {

    WorkshopMapper INSTANCE = Mappers.getMapper(WorkshopMapper.class);

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "state", source = "state")
    WorkshopResponse toResponse(Workshop workshop);

    @Mapping(target = "id", expression = "java(workshopId.value())")
    WorkshopResponse toResponseWithId(Workshop workshop, WorkshopId workshopId);

    default UUID map(WorkshopId value) {
        return value != null ? value.value() : null;
    }
}