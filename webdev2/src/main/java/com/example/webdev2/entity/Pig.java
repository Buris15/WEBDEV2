package com.example.webdev2.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pigs")
public class Pig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String tagNumber;

    @Column(nullable = false)
    private String name;

    private String breed;

    private Double weightKg;

    @OneToMany(
        mappedBy = "pig",
        fetch = FetchType.LAZY
    )
    private List<FeederAssignment> assignments =
            new ArrayList<>();

    public Pig() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTagNumber() {
        return tagNumber;
    }

    public void setTagNumber(String tagNumber) {
        this.tagNumber = tagNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public List<FeederAssignment> getAssignments() {
        return assignments;
    }

    public void addAssignment(FeederAssignment assignment) {
        assignments.add(assignment);
        assignment.setPig(this);
    }
}