package com.example.webdev2.service;

import com.example.webdev2.entity.*;
import com.example.webdev2.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class EnrollmentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository) {

        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public Enrollment enroll(
            Long studentId,
            Long courseId,
            String grade) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                    new RuntimeException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                    new RuntimeException("Course not found"));

        Enrollment enrollment = new Enrollment();

        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setGrade(grade);

        // Update both sides of the relationships
        student.addEnrollment(enrollment);
        course.addEnrollment(enrollment);

        // Save and immediately check database constraints
        return enrollmentRepository.saveAndFlush(enrollment);
    }
}