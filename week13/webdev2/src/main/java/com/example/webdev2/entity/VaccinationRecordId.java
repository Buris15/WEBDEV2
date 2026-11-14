package com.example.webdev2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class VaccinationRecordId implements Serializable {
    @Column(name = "pet_id")
    private Long petId;

    @Column(name = "vaccine_name", length = 100)
    private String vaccineName;

    @Column(name = "vaccination_date")
    private LocalDate vaccinationDate;

    public VaccinationRecordId() {}
    public VaccinationRecordId(Long petId, String vaccineName, LocalDate vaccinationDate) {
        this.petId = petId;
        this.vaccineName = vaccineName;
        this.vaccinationDate = vaccinationDate;
    }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }
    public LocalDate getVaccinationDate() { return vaccinationDate; }
    public void setVaccinationDate(LocalDate vaccinationDate) { this.vaccinationDate = vaccinationDate; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VaccinationRecordId that)) return false;
        return Objects.equals(petId, that.petId)
                && Objects.equals(vaccineName, that.vaccineName)
                && Objects.equals(vaccinationDate, that.vaccinationDate);
    }
    @Override public int hashCode() { return Objects.hash(petId, vaccineName, vaccinationDate); }
}
