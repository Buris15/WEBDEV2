package com.example.webdev2.repository;

import com.example.webdev2.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository
        extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentNumber(String studentNumber);
}