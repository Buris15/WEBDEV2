package com.example.webdev2.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PetWriteRequest(
        @NotBlank @Size(max = 50) String microchipNumber,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 50) String species,
        @NotBlank @Size(max = 100) String breed,
        @NotNull @Min(0) Integer age,
        @NotNull Long ownerId
) {}
