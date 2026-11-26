package com.example.webdev2.repository;

import com.example.webdev2.entity.VetAvailability;
import com.example.webdev2.entity.VetAvailabilityId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VetAvailabilityRepository extends JpaRepository<VetAvailability, VetAvailabilityId> {
    List<VetAvailability> findByIdAvailabilityDate(LocalDate availabilityDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT va FROM VetAvailability va JOIN FETCH va.veterinarian "
            + "WHERE va.id.vetId = :vetId AND va.id.availabilityDate = :date")
    Optional<VetAvailability> lockByVetAndDate(@Param("vetId") Long vetId,
                                               @Param("date") LocalDate date);
}
