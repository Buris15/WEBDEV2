package com.example.webdev2.service;

import com.example.webdev2.dto.*;
import com.example.webdev2.entity.Pig;
import com.example.webdev2.repository.PigRepository;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class PigService {

    private final PigRepository pigRepository;

    public PigService(PigRepository pigRepository) {
        this.pigRepository = pigRepository;
    }

    // ENTITY TO DTO
    public PigDTO toDTO(Pig pig) {
        return new PigDTO(
                pig.getId(),
                pig.getTagNumber(),
                pig.getName(),
                pig.getBreed(),
                pig.getWeightKg()
        );
    }

    // GET ALL
    public List<PigDTO> findAll() {
        return pigRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // GET BY ID
    public Optional<PigDTO> findById(Long id) {
        return pigRepository.findById(id)
                .map(this::toDTO);
    }

    // POST: CREATE
    public PigDTO create(CreatePigRequest request) {

        if (request.getTagNumber() == null ||
                request.getTagNumber().isBlank() ||
                request.getName() == null ||
                request.getName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tag number and name are required"
            );
        }

        // Check duplicate tag number
        if (pigRepository.existsByTagNumber(
                request.getTagNumber())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pig tag number already exists"
            );
        }

        Pig pig = new Pig();

        pig.setTagNumber(request.getTagNumber());
        pig.setName(request.getName());
        pig.setBreed(request.getBreed());
        pig.setWeightKg(request.getWeightKg());

        Pig savedPig = pigRepository.save(pig);

        return toDTO(savedPig);
    }

    // PUT: FULL UPDATE
    public Optional<PigDTO> update(
            Long id,
            UpdatePigRequest request) {

        if (request.getTagNumber() == null ||
                request.getTagNumber().isBlank() ||
                request.getName() == null ||
                request.getName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tag number and name are required"
            );
        }

        return pigRepository.findById(id).map(pig -> {

            if (pigRepository.existsByTagNumberAndIdNot(
                    request.getTagNumber(), id)) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Pig tag number already exists"
                );
            }

            pig.setTagNumber(request.getTagNumber());
            pig.setName(request.getName());
            pig.setBreed(request.getBreed());
            pig.setWeightKg(request.getWeightKg());

            return toDTO(pigRepository.save(pig));
        });
    }

    // PATCH: PARTIAL UPDATE
    public Optional<PigDTO> patch(
            Long id,
            UpdatePigRequest request) {

        if (request.getTagNumber() == null &&
                request.getName() == null &&
                request.getBreed() == null &&
                request.getWeightKg() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one field must be provided"
            );
        }

        return pigRepository.findById(id).map(pig -> {

            if (request.getTagNumber() != null) {

                if (request.getTagNumber().isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Tag number cannot be blank"
                    );
                }

                if (pigRepository.existsByTagNumberAndIdNot(
                        request.getTagNumber(), id)) {

                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Pig tag number already exists"
                    );
                }

                pig.setTagNumber(request.getTagNumber());
            }

            if (request.getName() != null) {

                if (request.getName().isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Name cannot be blank"
                    );
                }

                pig.setName(request.getName());
            }

            if (request.getBreed() != null) {
                pig.setBreed(request.getBreed());
            }

            if (request.getWeightKg() != null) {
                pig.setWeightKg(request.getWeightKg());
            }

            return toDTO(pigRepository.save(pig));
        });
    }

    // DELETE
    public boolean delete(Long id) {

        if (!pigRepository.existsById(id)) {
            return false;
        }

        pigRepository.deleteById(id);

        return true;
    }
}