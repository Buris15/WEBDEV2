package com.example.webdev2.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AppointmentWriteRequest(
        @NotNull @FutureOrPresent LocalDate appointmentDate,
        @NotBlank @Size(max = 255) String reasonForVisit,
        @NotNull Long petId,
        @NotNull Long vetId
) {}
