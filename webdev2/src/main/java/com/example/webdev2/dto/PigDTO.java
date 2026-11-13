package com.example.webdev2.dto;

public class PigDTO {

    private Long id;
    private String tagNumber;
    private String name;
    private String breed;
    private Double weightKg;

    public PigDTO() {
    }

    public PigDTO(
            Long id,
            String tagNumber,
            String name,
            String breed,
            Double weightKg) {

        this.id = id;
        this.tagNumber = tagNumber;
        this.name = name;
        this.breed = breed;
        this.weightKg = weightKg;
    }

    public Long getId() {
        return id;
    }

    public String getTagNumber() {
        return tagNumber;
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTagNumber(String tagNumber) {
        this.tagNumber = tagNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }
}