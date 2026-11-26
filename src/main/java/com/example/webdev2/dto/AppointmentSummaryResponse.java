package com.example.webdev2.dto;

import java.time.LocalDate;

public record AppointmentSummaryResponse(
        Long appointmentId,
        LocalDate appointmentDate,
        String reasonForVisit,
        String veterinarianName
) {}
