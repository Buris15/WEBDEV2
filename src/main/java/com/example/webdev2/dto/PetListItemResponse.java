package com.example.webdev2.dto;

public record PetListItemResponse(
        Long id,
        String microchipNumber,
        String name,
        String species,
        String breed,
        Integer age,
        String ownerName
) {}
