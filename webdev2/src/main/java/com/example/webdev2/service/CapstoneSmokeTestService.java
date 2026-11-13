package com.example.webdev2.service;

import com.example.webdev2.entity.*;
import com.example.webdev2.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class CapstoneSmokeTestService {

    private final PigRepository pigRepository;
    private final FeederRepository feederRepository;
    private final FeedingScheduleRepository scheduleRepository;
    private final FeederAssignmentRepository assignmentRepository;

    public CapstoneSmokeTestService(
            PigRepository pigRepository,
            FeederRepository feederRepository,
            FeedingScheduleRepository scheduleRepository,
            FeederAssignmentRepository assignmentRepository) {

        this.pigRepository = pigRepository;
        this.feederRepository = feederRepository;
        this.scheduleRepository = scheduleRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public void runSmokeTest() {

        System.out.println("\n===== CAPSTONE SMOKE TEST =====");

        // 1. Create or retrieve Pig
        Pig pig = pigRepository.findByTagNumber("PIG-001")
                .orElseGet(() -> {

                    Pig newPig = new Pig();

                    newPig.setTagNumber("PIG-001");
                    newPig.setName("Bantay");
                    newPig.setBreed("Landrace");
                    newPig.setWeightKg(45.5);

                    return pigRepository.save(newPig);
                });

        // 2. Create or retrieve Feeder
        Feeder feeder = feederRepository
                .findByFeederCode("FEEDER-001")
                .orElseGet(() -> {

                    Feeder newFeeder = new Feeder();

                    newFeeder.setFeederCode("FEEDER-001");
                    newFeeder.setLocation("Pig Pen 1");
                    newFeeder.setStatus("ACTIVE");

                    // Create feeding schedule
                    FeedingSchedule schedule =
                            new FeedingSchedule();

                    schedule.setFeedingTime(
                            LocalTime.of(6, 0));

                    schedule.setPortionGrams(500);
                    schedule.setActive(true);

                    // Keep both sides synchronized
                    newFeeder.addSchedule(schedule);

                    // Saves feeder and schedule together
                    return feederRepository.save(newFeeder);
                });

        // 3. Create assignment if it does not exist
        if (assignmentRepository
                .findByPig_IdAndFeeder_Id(
                        pig.getId(),
                        feeder.getId())
                .isEmpty()) {

            FeederAssignment assignment =
                    new FeederAssignment();

            assignment.setAssignedAt(LocalDate.now());
            assignment.setActive(true);

            pig.addAssignment(assignment);
            feeder.addAssignment(assignment);

            assignmentRepository.save(assignment);
        }

        // Ensure database statements are executed
        assignmentRepository.flush();

        // 4. Read back the saved information
        Pig savedPig = pigRepository
                .findByTagNumber("PIG-001")
                .orElseThrow();

        Feeder savedFeeder = feederRepository
                .findByFeederCode("FEEDER-001")
                .orElseThrow();

        System.out.println("Pig saved successfully!");
        System.out.println("Pig Tag: " + savedPig.getTagNumber());
        System.out.println("Pig Name: " + savedPig.getName());
        System.out.println("Breed: " + savedPig.getBreed());

        System.out.println("\nFeeder saved successfully!");
        System.out.println("Feeder Code: "
                + savedFeeder.getFeederCode());

        System.out.println("Location: "
                + savedFeeder.getLocation());

        // Read feeding schedules
        System.out.println("\n===== FEEDING SCHEDULE =====");

        scheduleRepository
                .findByFeeder_Id(savedFeeder.getId())
                .forEach(schedule -> {

                    System.out.println(
                            "Time: " + schedule.getFeedingTime());

                    System.out.println(
                            "Portion: "
                            + schedule.getPortionGrams()
                            + " grams");

                    System.out.println(
                            "Active: " + schedule.getActive());
                });

        System.out.println("\nFEEDER ASSIGNMENT SAVED: "
                + assignmentRepository
                    .findByPig_IdAndFeeder_Id(
                            savedPig.getId(),
                            savedFeeder.getId())
                    .isPresent());

        System.out.println("\nCAPSTONE DATABASE TEST SUCCESSFUL!");
    }
}