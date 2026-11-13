package com.example.webdev2;

import com.example.webdev2.entity.Pet;
import com.example.webdev2.repository.PetRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PetSmokeTestRunner implements CommandLineRunner {

    private final PetRepository petRepository;

    public PetSmokeTestRunner(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Override
    public void run(String... args) {

        // Add sample pet if the table is empty
        if (petRepository.count() == 0) {

            Pet pet = new Pet();

            pet.setMicrochipNumber("CHIP-0001");
            pet.setName("Max");
            pet.setSpecies("Dog");
            pet.setBreed("Golden Retriever");

            petRepository.save(pet);
        }

        // Display all pets
        System.out.println("===== PET RECORDS =====");

        petRepository.findAll().forEach(pet -> {
            System.out.println("ID: " + pet.getId());
            System.out.println("Name: " + pet.getName());
            System.out.println("Species: " + pet.getSpecies());
            System.out.println("Breed: " + pet.getBreed());
            System.out.println("-----------------------");
        });
    }
}