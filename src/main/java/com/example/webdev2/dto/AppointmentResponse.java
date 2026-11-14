package com.example.webdev2.dto;

import java.time.LocalDate;

public record AppointmentResponse(
        Long id,
        LocalDate appointmentDate,
        String reasonForVisit,
        Long petId,
        String petName,
        Long vetId,
        String veterinarianName
) {}
