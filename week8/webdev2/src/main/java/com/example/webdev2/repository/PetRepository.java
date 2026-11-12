package com.example.webdev2.repository;

import com.example.webdev2.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {

    // Search pets by species
    List<Pet> findBySpeciesIgnoreCase(String species);

    // Search pets by name
    List<Pet> findByNameContainingIgnoreCase(String keyword);

    // Get all distinct species
    @Query("SELECT DISTINCT p.species FROM Pet p")
    List<String> findAllSpecies();
}