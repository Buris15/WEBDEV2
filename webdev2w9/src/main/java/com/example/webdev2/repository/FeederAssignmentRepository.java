package com.example.webdev2.repository;

import com.example.webdev2.entity.FeederAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeederAssignmentRepository
        extends JpaRepository<FeederAssignment, Long> {

    Optional<FeederAssignment> findByPig_IdAndFeeder_Id(
            Long pigId,
            Long feederId
    );
}