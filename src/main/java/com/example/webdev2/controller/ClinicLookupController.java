package com.example.webdev2.controller;

import com.example.webdev2.dto.OwnerOptionResponse;
import com.example.webdev2.dto.VeterinarianOptionResponse;
import com.example.webdev2.dto.VetAvailabilityResponse;
import com.example.webdev2.entity.VetAvailability;
import com.example.webdev2.repository.OwnerRepository;
import com.example.webdev2.repository.VeterinarianRepository;
import com.example.webdev2.repository.VetAvailabilityRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@RestController
public class ClinicLookupController {
    private final OwnerRepository ownerRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final VetAvailabilityRepository availabilityRepository;

    public ClinicLookupController(OwnerRepository ownerRepository,
                                  VeterinarianRepository veterinarianRepository,
                                  VetAvailabilityRepository availabilityRepository) {
        this.ownerRepository = ownerRepository;
        this.veterinarianRepository = veterinarianRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @GetMapping("/api/owners")
    public ResponseEntity<List<OwnerOptionResponse>> owners() {
        return ResponseEntity.ok(ownerRepository.findAll(Sort.by("lastName").ascending().and(Sort.by("firstName")))
                .stream().map(owner -> new OwnerOptionResponse(owner.getId(), owner.getFullName(), owner.getPhoneNumber()))
                .toList());
    }

    @GetMapping("/api/veterinarians")
    public ResponseEntity<List<VeterinarianOptionResponse>> veterinarians() {
        return ResponseEntity.ok(veterinarianRepository.findAll(
                        Sort.by("lastName").ascending().and(Sort.by("firstName")))
                .stream().map(vet -> new VeterinarianOptionResponse(
                        vet.getId(), vet.getFullName(), vet.getSpecialization()))
                .toList());
    }

    @GetMapping("/api/vet-availability")
    public ResponseEntity<List<VetAvailabilityResponse>> availability(@RequestParam LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose today or a future date.");
        }
        List<VetAvailability> rows = availabilityRepository.findByIdAvailabilityDate(date);
        rows.sort(Comparator.comparing(row -> row.getVeterinarian().getLastName()));
        return ResponseEntity.ok(rows.stream().map(row -> new VetAvailabilityResponse(
                row.getVeterinarian().getId(), row.getVeterinarian().getFullName(),
                row.getVeterinarian().getSpecialization(), row.getId().getAvailabilityDate(),
                row.getTotalSlots(), row.getAvailableSlots())).toList());
    }
}
