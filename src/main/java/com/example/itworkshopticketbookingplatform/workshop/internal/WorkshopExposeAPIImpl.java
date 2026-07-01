package com.example.itworkshopticketbookingplatform.workshop.internal;

import org.springframework.stereotype.Service;
import com.example.itworkshopticketbookingplatform.workshop.WorkshopExposeAPI;

@Service
class WorkshopExposeAPIImpl implements WorkshopExposeAPI {

    private final WorkshopService workshopService;

    WorkshopExposeAPIImpl(WorkshopService workshopService) {
        this.workshopService = workshopService;
    }
}
