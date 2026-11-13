package com.example.webdev2.repository;

import com.example.webdev2.entity.FeedingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedingScheduleRepository
        extends JpaRepository<FeedingSchedule, Long> {

    List<FeedingSchedule> findByFeeder_Id(Long feederId);
}