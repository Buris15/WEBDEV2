package com.example.webdev2.dto;

import java.time.LocalDate;

public record VetAvailabilityResponse(
        Long vetId,
        String veterinarianName,
        String specialization,
        LocalDate availabilityDate,
        Integer totalSlots,
        Integer availableSlots
) {}
