package com.example.webdev2.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vet_availability")
public class VetAvailability {
    @EmbeddedId
    private VetAvailabilityId id;

    @MapsId("vetId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vet_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_vet_availability_veterinarian"))
    private Veterinarian veterinarian;

    @Column(name = "total_slots", nullable = false)
    private Integer totalSlots;

    @Column(name = "available_slots", nullable = false)
    private Integer availableSlots;

    public VetAvailability() {}
    public VetAvailabilityId getId() { return id; }
    public void setId(VetAvailabilityId id) { this.id = id; }
    public Veterinarian getVeterinarian() { return veterinarian; }
    public void setVeterinarian(Veterinarian veterinarian) { this.veterinarian = veterinarian; }
    public Integer getTotalSlots() { return totalSlots; }
    public void setTotalSlots(Integer totalSlots) { this.totalSlots = totalSlots; }
    public Integer getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(Integer availableSlots) { this.availableSlots = availableSlots; }
}
