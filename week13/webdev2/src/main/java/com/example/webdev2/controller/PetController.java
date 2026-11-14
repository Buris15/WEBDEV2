package com.example.webdev2.controller;

import com.example.webdev2.dto.PetResponse;
import com.example.webdev2.dto.PetWriteRequest;
import com.example.webdev2.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {
    private final PetService petService;

    public PetController(PetService petService) { this.petService = petService; }

    @PostMapping
    public ResponseEntity<PetResponse> registerPet(@Valid @RequestBody PetWriteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(petService.registerPet(request));
    }

    @GetMapping
    public ResponseEntity<List<PetResponse>> getAllPets() {
        return ResponseEntity.ok(petService.getAllPets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponse> getPetById(@PathVariable Long id) {
        return ResponseEntity.ok(petService.getPetById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> updatePet(@PathVariable Long id,
                                                  @Valid @RequestBody PetWriteRequest request) {
        return ResponseEntity.ok(petService.updatePet(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removePet(@PathVariable Long id) {
        petService.removePet(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/species")
    public ResponseEntity<List<PetResponse>> getPetsBySpecies(@RequestParam String name) {
        return ResponseEntity.ok(petService.getPetsBySpecies(name));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PetResponse>> searchPets(@RequestParam String keyword) {
        return ResponseEntity.ok(petService.searchPetsByName(keyword));
    }

    @GetMapping("/species-list")
    public ResponseEntity<List<String>> getAllSpecies() {
        return ResponseEntity.ok(petService.getAllSpecies());
    }

    @PostMapping("/batch")
    public ResponseEntity<String> batchRegister(@Valid @RequestBody List<@Valid PetWriteRequest> requests) {
        petService.batchRegister(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body("Pets registered successfully!");
    }
}
