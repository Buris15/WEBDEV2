package com.example.webdev2.controller;

import com.example.webdev2.dto.PetDetailResponse;
import com.example.webdev2.dto.PetListItemResponse;
import com.example.webdev2.service.PetReadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pets")
public class PetReadController {
    private final PetReadService petReadService;

    public PetReadController(PetReadService petReadService) { this.petReadService = petReadService; }

    @GetMapping
    public ResponseEntity<Page<PetListItemResponse>> listPets(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(petReadService.listPets(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetDetailResponse> getPetDetails(@PathVariable Long id) {
        return ResponseEntity.ok(petReadService.getPetDetails(id));
    }
}
