package com.example.webdev2.controller;

import com.example.webdev2.dto.*;
import com.example.webdev2.service.PigService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pigs")
public class PigController {

    private final PigService pigService;

    public PigController(PigService pigService) {
        this.pigService = pigService;
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<PigDTO>> getAllPigs() {

        return ResponseEntity.ok(pigService.findAll());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<PigDTO> getPigById(
            @PathVariable Long id) {

        return pigService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST
    @PostMapping
    public ResponseEntity<PigDTO> createPig(
            @RequestBody CreatePigRequest request) {

        PigDTO created = pigService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(created);
    }

    // PUT
    @PutMapping("/{id}")
    public ResponseEntity<PigDTO> updatePig(
            @PathVariable Long id,
            @RequestBody UpdatePigRequest request) {

        return pigService.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PATCH
    @PatchMapping("/{id}")
    public ResponseEntity<PigDTO> patchPig(
            @PathVariable Long id,
            @RequestBody UpdatePigRequest request) {

        return pigService.patch(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePig(
            @PathVariable Long id) {

        if (pigService.delete(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    // HANDLE 400 AND 409 RESPONSES
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleErrors(
            ResponseStatusException exception) {

        String message = exception.getReason() != null
                ? exception.getReason()
                : "Request failed";

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(Map.of("message", message));
    }
}