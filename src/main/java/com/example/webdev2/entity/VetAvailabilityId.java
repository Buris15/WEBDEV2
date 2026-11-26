package com.example.webdev2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class VetAvailabilityId implements Serializable {
    @Column(name = "vet_id")
    private Long vetId;

    @Column(name = "availability_date")
    private LocalDate availabilityDate;

    public VetAvailabilityId() {}

    public VetAvailabilityId(Long vetId, LocalDate availabilityDate) {
        this.vetId = vetId;
        this.availabilityDate = availabilityDate;
    }

    public Long getVetId() { return vetId; }
    public void setVetId(Long vetId) { this.vetId = vetId; }
    public LocalDate getAvailabilityDate() { return availabilityDate; }
    public void setAvailabilityDate(LocalDate availabilityDate) { this.availabilityDate = availabilityDate; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof VetAvailabilityId that)) return false;
        return Objects.equals(vetId, that.vetId)
                && Objects.equals(availabilityDate, that.availabilityDate);
    }

    @Override
    public int hashCode() { return Objects.hash(vetId, availabilityDate); }
}
