package com.example.webdev2.dto;

import java.util.List;

public record PetDetailResponse(
        Long id,
        String microchipNumber,
        String name,
        String species,
        String breed,
        Integer age,
        String ownerName,
        String ownerPhoneNumber,
        List<AppointmentSummaryResponse> appointments,
        List<VaccinationSummaryResponse> vaccinations
) {}
