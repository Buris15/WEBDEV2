package com.example.webdev2.repository;

import com.example.webdev2.entity.Pig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PigRepository extends JpaRepository<Pig, Long> {

    Optional<Pig> findByTagNumber(String tagNumber);

    boolean existsByTagNumber(String tagNumber);

    boolean existsByTagNumberAndIdNot(
            String tagNumber,
            Long id
    );
}