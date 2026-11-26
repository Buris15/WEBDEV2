package com.example.webdev2.service;

import com.example.webdev2.dto.AppointmentResponse;
import com.example.webdev2.dto.AppointmentWriteRequest;
import com.example.webdev2.entity.Appointment;
import com.example.webdev2.entity.Pet;
import com.example.webdev2.entity.VetAvailability;
import com.example.webdev2.entity.VetAvailabilityId;
import com.example.webdev2.entity.Veterinarian;
import com.example.webdev2.repository.AppointmentRepository;
import com.example.webdev2.repository.PetRepository;
import com.example.webdev2.repository.VetAvailabilityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PetRepository petRepository;
    private final VetAvailabilityRepository availabilityRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PetRepository petRepository,
                              VetAvailabilityRepository availabilityRepository) {
        this.appointmentRepository = appointmentRepository;
        this.petRepository = petRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAllByOrderByAppointmentDateAscIdAsc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(Long id) {
        return toResponse(findAppointment(id));
    }

    /**
     * One booking transaction reserves a veterinarian/date slot and inserts the appointment.
     * The availability row is locked before it is checked and changed. Looking up the pet after
     * the first write also means a missing pet exercises the rollback path in a controlled test.
     */
    @Transactional
    public AppointmentResponse bookAppointment(AppointmentWriteRequest request) {
        VetAvailability slot = lockSlot(request.vetId(), request.appointmentDate());
        requireCapacity(slot);

        slot.setAvailableSlots(slot.getAvailableSlots() - 1);
        availabilityRepository.saveAndFlush(slot);

        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setReasonForVisit(request.reasonForVisit().trim());
        appointment.setPet(pet);
        appointment.setVeterinarian(slot.getVeterinarian());
        return toResponse(appointmentRepository.saveAndFlush(appointment));
    }

    /**
     * A PUT replaces all editable fields. When the veterinarian/date changes, both schedule rows
     * are locked in a stable order, the new slot is reserved, and the old slot is released in this
     * same transaction. A failed later write rolls both updates back.
     */
    @Transactional
    public AppointmentResponse updateAppointment(Long id, AppointmentWriteRequest request) {
        Appointment appointment = findAppointment(id);
        VetAvailabilityId oldKey = new VetAvailabilityId(
                appointment.getVeterinarian().getId(), appointment.getAppointmentDate());
        VetAvailabilityId newKey = new VetAvailabilityId(request.vetId(), request.appointmentDate());

        VetAvailability oldSlot;
        VetAvailability newSlot;
        if (oldKey.equals(newKey)) {
            oldSlot = lockSlot(oldKey.getVetId(), oldKey.getAvailabilityDate());
            newSlot = oldSlot;
        } else {
            Map<VetAvailabilityId, VetAvailability> locked = lockSlotsInStableOrder(oldKey, newKey);
            oldSlot = locked.get(oldKey);
            newSlot = locked.get(newKey);
            if (oldSlot == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "The current appointment schedule row is missing; ask an administrator to review it.");
            }
            requireCapacity(newSlot);
            newSlot.setAvailableSlots(newSlot.getAvailableSlots() - 1);
            oldSlot.setAvailableSlots(oldSlot.getAvailableSlots() + 1);
            if (oldSlot.getAvailableSlots() > oldSlot.getTotalSlots()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "The old schedule capacity is already full; the appointment cannot be moved safely.");
            }
            availabilityRepository.saveAndFlush(newSlot);
            availabilityRepository.saveAndFlush(oldSlot);
        }

        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));
        Veterinarian veterinarian = newSlot.getVeterinarian();
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setReasonForVisit(request.reasonForVisit().trim());
        appointment.setPet(pet);
        appointment.setVeterinarian(veterinarian);
        return toResponse(appointmentRepository.saveAndFlush(appointment));
    }

    /** Cancelling an appointment deletes it and restores its reserved slot atomically. */
    @Transactional
    public void cancelAppointment(Long id) {
        Appointment appointment = findAppointment(id);
        VetAvailability slot = lockSlot(appointment.getVeterinarian().getId(), appointment.getAppointmentDate());
        if (slot.getAvailableSlots() >= slot.getTotalSlots()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The schedule is already at full capacity; cancellation was not applied.");
        }
        slot.setAvailableSlots(slot.getAvailableSlots() + 1);
        availabilityRepository.saveAndFlush(slot);
        appointmentRepository.delete(appointment);
        appointmentRepository.flush();
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findWithRelationsById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private VetAvailability lockSlot(Long vetId, java.time.LocalDate date) {
        return availabilityRepository.lockByVetAndDate(vetId, date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "No schedule has been configured for this veterinarian on the selected date."));
    }

    private void requireCapacity(VetAvailability slot) {
        if (slot == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No schedule has been configured for this veterinarian on the selected date.");
        }
        if (slot.getAvailableSlots() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The selected veterinarian has no available appointment slots on this date.");
        }
    }

    private Map<VetAvailabilityId, VetAvailability> lockSlotsInStableOrder(
            VetAvailabilityId first, VetAvailabilityId second) {
        List<VetAvailabilityId> keys = new ArrayList<>(List.of(first, second));
        keys.sort(Comparator.comparing(VetAvailabilityId::getAvailabilityDate)
                .thenComparing(VetAvailabilityId::getVetId));
        Map<VetAvailabilityId, VetAvailability> locked = new LinkedHashMap<>();
        for (VetAvailabilityId key : keys) {
            VetAvailability slot = availabilityRepository
                    .lockByVetAndDate(key.getVetId(), key.getAvailabilityDate()).orElse(null);
            if (slot != null) locked.put(key, slot);
        }
        return locked;
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(), appointment.getAppointmentDate(), appointment.getReasonForVisit(),
                appointment.getPet().getId(), appointment.getPet().getName(),
                appointment.getVeterinarian().getId(), appointment.getVeterinarian().getFullName());
    }
}
