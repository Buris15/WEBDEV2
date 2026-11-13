package com.example.webdev2.service;

import com.example.webdev2.entity.Pet;
import com.example.webdev2.repository.PetRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PetService {

    private final PetRepository petRepository;

    // Constructor injection
    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    // CREATE
    public Pet registerPet(Pet pet) {

        if (pet.getMicrochipNumber() == null ||
                pet.getMicrochipNumber().isBlank()) {
           throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Microchip number is required");
        }

        if (pet.getName() == null || pet.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Pet name is required");
        }

        return petRepository.save(pet);
    }

    // READ ALL
    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    // READ ONE
    public Pet getPetById(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pet not found"));
    }

    // UPDATE
    public Pet updatePet(Long id, Pet updated) {

        Pet existingPet = getPetById(id);

        existingPet.setMicrochipNumber(updated.getMicrochipNumber());
        existingPet.setName(updated.getName());
        existingPet.setSpecies(updated.getSpecies());
        existingPet.setBreed(updated.getBreed());

        return petRepository.save(existingPet);
    }

    // DELETE
    public void removePet(Long id) {

        if (!petRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Pet not found");
        }

        petRepository.deleteById(id);
    }

    // Search by species
public List<Pet> getPetsBySpecies(String species) {
    return petRepository.findBySpeciesIgnoreCase(species);
}

// Search by name
public List<Pet> searchPetsByName(String keyword) {
    return petRepository.findByNameContainingIgnoreCase(keyword);
}

// Get all distinct species
public List<String> getAllSpecies() {
    return petRepository.findAllSpecies();
}
// BATCH REGISTER WITH TRANSACTION
@Transactional
public void batchRegister(List<Pet> pets) {

    for (Pet pet : pets) {
        petRepository.save(pet);
    }

}
}