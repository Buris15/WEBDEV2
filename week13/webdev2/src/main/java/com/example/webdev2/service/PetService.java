package com.example.webdev2.service;

import com.example.webdev2.dto.PetResponse;
import com.example.webdev2.dto.PetWriteRequest;
import com.example.webdev2.entity.Owner;
import com.example.webdev2.entity.Pet;
import com.example.webdev2.repository.OwnerRepository;
import com.example.webdev2.repository.PetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class PetService {
    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    public PetService(PetRepository petRepository, OwnerRepository ownerRepository) {
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
    }

    @Transactional
    public PetResponse registerPet(PetWriteRequest request) {
        Pet pet = new Pet();
        applyRequest(pet, request);
        return toResponse(petRepository.save(pet));
    }

    @Transactional(readOnly = true)
    public List<PetResponse> getAllPets() {
        return petRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PetResponse getPetById(Long id) {
        return toResponse(findPet(id));
    }

    @Transactional
    public PetResponse updatePet(Long id, PetWriteRequest request) {
        Pet pet = findPet(id);
        applyRequest(pet, request);
        return toResponse(petRepository.save(pet));
    }

    @Transactional
    public void removePet(Long id) {
        Pet pet = findPet(id);
        try {
            petRepository.delete(pet);
            petRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Pet has appointment or vaccination records and cannot be deleted yet.");
        }
    }

    @Transactional(readOnly = true)
    public List<PetResponse> getPetsBySpecies(String species) {
        return petRepository.findBySpeciesIgnoreCase(species).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PetResponse> searchPetsByName(String keyword) {
        return petRepository.findByNameContainingIgnoreCase(keyword).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<String> getAllSpecies() {
        return petRepository.findAllSpecies();
    }

    @Transactional
    public void batchRegister(List<PetWriteRequest> requests) {
        for (PetWriteRequest request : requests) {
            Pet pet = new Pet();
            applyRequest(pet, request);
            petRepository.save(pet);
        }
    }

    private Pet findPet(Long id) {
        return petRepository.findWithOwnerById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));
    }

    private void applyRequest(Pet pet, PetWriteRequest request) {
        Owner owner = ownerRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner does not exist"));
        pet.setMicrochipNumber(request.microchipNumber().trim());
        pet.setName(request.name().trim());
        pet.setSpecies(request.species().trim());
        pet.setBreed(request.breed().trim());
        pet.setAge(request.age());
        pet.setOwner(owner);
    }

    private PetResponse toResponse(Pet pet) {
        return new PetResponse(pet.getId(), pet.getMicrochipNumber(), pet.getName(), pet.getSpecies(),
                pet.getBreed(), pet.getAge(), pet.getOwner() == null ? null : pet.getOwner().getId());
    }
}
