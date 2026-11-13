package com.example.webdev2.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "feeding_schedules")
public class FeedingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalTime feedingTime;

    @Column(nullable = false)
    private Integer portionGrams;

    private Boolean active;

    // Owning side of Many-to-One
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feeder_id", nullable = false)
    private Feeder feeder;

    public FeedingSchedule() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalTime getFeedingTime() {
        return feedingTime;
    }

    public void setFeedingTime(LocalTime feedingTime) {
        this.feedingTime = feedingTime;
    }

    public Integer getPortionGrams() {
        return portionGrams;
    }

    public void setPortionGrams(Integer portionGrams) {
        this.portionGrams = portionGrams;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Feeder getFeeder() {
        return feeder;
    }

    public void setFeeder(Feeder feeder) {
        this.feeder = feeder;
    }
}