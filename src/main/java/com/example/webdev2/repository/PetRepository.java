package com.example.webdev2.repository;

import com.example.webdev2.entity.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {
    @Override
    @EntityGraph(attributePaths = "owner")
    Page<Pet> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "owner")
    List<Pet> findAll();

    @EntityGraph(attributePaths = "owner")
    Optional<Pet> findWithOwnerById(Long id);

    List<Pet> findBySpeciesIgnoreCase(String species);
    List<Pet> findByNameContainingIgnoreCase(String keyword);

    @Query("SELECT DISTINCT p.species FROM Pet p ORDER BY p.species")
    List<String> findAllSpecies();
}
