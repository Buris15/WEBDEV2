package com.example.webdev2.dto;

public record PetResponse(
        Long id,
        String microchipNumber,
        String name,
        String species,
        String breed,
        Integer age,
        Long ownerId
) {}
