package com.example.itworkshopticketbookingplatform.workshop.internal.application.mapper;

import com.example.itworkshopticketbookingplatform.workshop.WorkshopRequest;
import com.example.itworkshopticketbookingplatform.workshop.WorkshopResponse;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.Workshop;
import com.example.itworkshopticketbookingplatform.workshop.internal.domain.model.WorkshopId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface WorkshopMapper {

    WorkshopMapper INSTANCE = Mappers.getMapper(WorkshopMapper.class);

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "state", source = "state")
    WorkshopResponse toResponse(Workshop workshop);

    @Mapping(target = "id", source = "workshopId")
    WorkshopResponse toResponseWithId(Workshop workshop, WorkshopId workshopId);
}