package com.example.webdev2.repository;

import com.example.webdev2.entity.Feeder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeederRepository
        extends JpaRepository<Feeder, Long> {

    Optional<Feeder> findByFeederCode(String feederCode);
}