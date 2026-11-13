package com.example.webdev2.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feeders")
public class Feeder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String feederCode;

    private String location;

    private String status;

    // One Feeder has many FeedingSchedules
    @OneToMany(
        mappedBy = "feeder",
        fetch = FetchType.LAZY,
        cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
        },
        orphanRemoval = true
    )
    private List<FeedingSchedule> schedules =
            new ArrayList<>();

    // One Feeder has many assignments
    @OneToMany(
        mappedBy = "feeder",
        fetch = FetchType.LAZY
    )
    private List<FeederAssignment> assignments =
            new ArrayList<>();

    public Feeder() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFeederCode() {
        return feederCode;
    }

    public void setFeederCode(String feederCode) {
        this.feederCode = feederCode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<FeedingSchedule> getSchedules() {
        return schedules;
    }

    public List<FeederAssignment> getAssignments() {
        return assignments;
    }

    public void addSchedule(FeedingSchedule schedule) {
        schedules.add(schedule);
        schedule.setFeeder(this);
    }

    public void addAssignment(FeederAssignment assignment) {
        assignments.add(assignment);
        assignment.setFeeder(this);
    }
}