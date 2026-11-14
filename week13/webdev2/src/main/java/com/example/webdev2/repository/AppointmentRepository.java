package com.example.webdev2.repository;

import com.example.webdev2.entity.Appointment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @EntityGraph(attributePaths = "veterinarian")
    List<Appointment> findByPet_IdOrderByAppointmentDateDesc(Long petId);
}
