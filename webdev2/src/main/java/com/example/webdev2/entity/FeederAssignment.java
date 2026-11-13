package com.example.webdev2.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "feeder_assignments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_pig_feeder",
            columnNames = {"pig_id", "feeder_id"}
        )
    }
)
public class FeederAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate assignedAt;

    private Boolean active;

    // Owning side: Pig relationship
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pig_id", nullable = false)
    private Pig pig;

    // Owning side: Feeder relationship
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feeder_id", nullable = false)
    private Feeder feeder;

    public FeederAssignment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDate assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Pig getPig() {
        return pig;
    }

    public void setPig(Pig pig) {
        this.pig = pig;
    }

    public Feeder getFeeder() {
        return feeder;
    }

    public void setFeeder(Feeder feeder) {
        this.feeder = feeder;
    }
}