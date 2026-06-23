package com.example.itworkshopticketbookingplatform;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithArchitectureTests {

    @Test
    void verifyModularStructure() {
        ApplicationModules.of(ItWorkshopTicketBookingPlatformApplication.class).verify();
    }
}