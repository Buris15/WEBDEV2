package com.example.webdev2.service;

import com.example.webdev2.dto.*;
import com.example.webdev2.entity.Appointment;
import com.example.webdev2.entity.Pet;
import com.example.webdev2.entity.VaccinationRecord;
import com.example.webdev2.repository.AppointmentRepository;
import com.example.webdev2.repository.PetRepository;
import com.example.webdev2.repository.VaccinationRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class PetReadService {
    private final PetRepository petRepository;
    private final AppointmentRepository appointmentRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;

    public PetReadService(PetRepository petRepository,
                          AppointmentRepository appointmentRepository,
                          VaccinationRecordRepository vaccinationRecordRepository) {
        this.petRepository = petRepository;
        this.appointmentRepository = appointmentRepository;
        this.vaccinationRecordRepository = vaccinationRecordRepository;
    }

    @Transactional(readOnly = true)
    public Page<PetListItemResponse> listPets(Pageable pageable) {
        return petRepository.findAll(pageable).map(this::toListItem);
    }

    @Transactional(readOnly = true)
    public PetDetailResponse getPetDetails(Long id) {
        Pet pet = petRepository.findWithOwnerById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        List<AppointmentSummaryResponse> appointments = appointmentRepository
                .findByPet_IdOrderByAppointmentDateDesc(id).stream()
                .map(this::toAppointmentSummary).toList();

        List<VaccinationSummaryResponse> vaccinations = vaccinationRecordRepository
                .findByPet_Id(id).stream().map(this::toVaccinationSummary).toList();

        return new PetDetailResponse(
                pet.getId(), pet.getMicrochipNumber(), pet.getName(), pet.getSpecies(),
                pet.getBreed(), pet.getAge(), pet.getOwner().getFullName(),
                pet.getOwner().getPhoneNumber(), appointments, vaccinations);
    }

    private PetListItemResponse toListItem(Pet pet) {
        return new PetListItemResponse(pet.getId(), pet.getMicrochipNumber(), pet.getName(),
                pet.getSpecies(), pet.getBreed(), pet.getAge(), pet.getOwner().getFullName());
    }

    private AppointmentSummaryResponse toAppointmentSummary(Appointment appointment) {
        return new AppointmentSummaryResponse(appointment.getId(), appointment.getAppointmentDate(),
                appointment.getReasonForVisit(), appointment.getVeterinarian().getFullName());
    }

    private VaccinationSummaryResponse toVaccinationSummary(VaccinationRecord record) {
        return new VaccinationSummaryResponse(record.getVaccineName(), record.getVaccinationDate());
    }
}
