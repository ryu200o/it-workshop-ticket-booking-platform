package com.example.itworkshopticketbookingplatform.room.presentation;

import jakarta.validation.constraints.NotNull;

public class RoomActivationRequest {
    @NotNull(message = "Active status cannot be null")
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
