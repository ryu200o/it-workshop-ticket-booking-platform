package com.example.itworkshopticketbookingplatform.registration.internal;

import com.example.itworkshopticketbookingplatform.registration.RegistrationExposeAPI;
import org.springframework.stereotype.Service;

@Service
class RegistrationExposeAPIImpl implements RegistrationExposeAPI {

    private final RegistrationService registrationService;

    RegistrationExposeAPIImpl(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }
}
