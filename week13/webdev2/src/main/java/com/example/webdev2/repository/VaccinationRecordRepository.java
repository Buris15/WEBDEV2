package com.example.webdev2.repository;

import com.example.webdev2.entity.VaccinationRecord;
import com.example.webdev2.entity.VaccinationRecordId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, VaccinationRecordId> {
    List<VaccinationRecord> findByPet_Id(Long petId);
}
