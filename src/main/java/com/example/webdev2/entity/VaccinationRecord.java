package com.example.webdev2.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vaccination_record")
public class VaccinationRecord {
    @EmbeddedId
    private VaccinationRecordId id;

    @MapsId("petId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_vaccination_pet"))
    private Pet pet;

    public VaccinationRecord() {}
    public VaccinationRecordId getId() { return id; }
    public void setId(VaccinationRecordId id) { this.id = id; }
    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }
    public String getVaccineName() { return id == null ? null : id.getVaccineName(); }
    public java.time.LocalDate getVaccinationDate() { return id == null ? null : id.getVaccinationDate(); }
}
