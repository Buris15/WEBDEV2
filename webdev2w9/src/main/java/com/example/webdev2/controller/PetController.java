package com.example.webdev2.controller;

import com.example.webdev2.entity.Pet;
import com.example.webdev2.service.PetService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    // CREATE PET
    @PostMapping
    public ResponseEntity<Pet> registerPet(@RequestBody Pet pet) {

        Pet savedPet = petService.registerPet(pet);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedPet);
    }

    // GET ALL PETS
    @GetMapping
    public ResponseEntity<List<Pet>> getAllPets() {

        return ResponseEntity.ok(petService.getAllPets());
    }

    // GET PET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Pet> getPetById(@PathVariable Long id) {

        return ResponseEntity.ok(petService.getPetById(id));
    }

    // UPDATE PET
    @PutMapping("/{id}")
    public ResponseEntity<Pet> updatePet(
            @PathVariable Long id,
            @RequestBody Pet pet) {

        return ResponseEntity.ok(
                petService.updatePet(id, pet));
    }

    // DELETE PET
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removePet(@PathVariable Long id) {

        petService.removePet(id);

        return ResponseEntity.noContent().build();
    }
    // SEARCH PETS BY SPECIES
@GetMapping("/species")
public ResponseEntity<List<Pet>> getPetsBySpecies(
        @RequestParam String name) {

    return ResponseEntity.ok(
            petService.getPetsBySpecies(name));
}

// SEARCH PETS BY NAME
@GetMapping("/search")
public ResponseEntity<List<Pet>> searchPets(
        @RequestParam String keyword) {

    return ResponseEntity.ok(
            petService.searchPetsByName(keyword));
}

// GET ALL DISTINCT SPECIES
@GetMapping("/species-list")
public ResponseEntity<List<String>> getAllSpecies() {

    return ResponseEntity.ok(
            petService.getAllSpecies());
}
// BATCH REGISTER PETS
@PostMapping("/batch")
public ResponseEntity<String> batchRegister(
        @RequestBody List<Pet> pets) {

    petService.batchRegister(pets);

    return ResponseEntity.status(HttpStatus.CREATED)
            .body("Pets registered successfully!");
}
}