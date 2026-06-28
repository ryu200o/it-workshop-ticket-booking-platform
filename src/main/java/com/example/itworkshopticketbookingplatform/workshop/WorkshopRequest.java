package com.example.itworkshopticketbookingplatform.workshop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkshopRequest(
    @NotBlank @Size(max = 200)
    String title,

    @Size(max = 2000)
    String description
) {}