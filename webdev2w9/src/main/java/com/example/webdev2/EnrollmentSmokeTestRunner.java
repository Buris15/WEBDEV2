package com.example.webdev2;

import com.example.webdev2.entity.*;
import com.example.webdev2.repository.*;
import com.example.webdev2.service.EnrollmentService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.dao.DataIntegrityViolationException;

@Component
public class EnrollmentSmokeTestRunner
        implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;

    public EnrollmentSmokeTestRunner(
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            EnrollmentService enrollmentService) {

        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentService = enrollmentService;
    }

    @Override
    public void run(String... args) {

        // Create or retrieve a sample student
        Student student = studentRepository
                .findByStudentNumber("WEEK9-STU-001")
                .orElseGet(() -> {

                    Student newStudent = new Student();

                    newStudent.setStudentNumber("WEEK9-STU-001");
                    newStudent.setName("Adrian");

                    return studentRepository.save(newStudent);
                });

        // Create or retrieve a sample course
        Course course = courseRepository
                .findByCourseCode("WEBDEV-09")
                .orElseGet(() -> {

                    Course newCourse = new Course();

                    newCourse.setCourseCode("WEBDEV-09");
                    newCourse.setTitle("Advanced Web Development");

                    return courseRepository.save(newCourse);
                });

        System.out.println("\n===== ENROLLMENT TEST =====");

        // Remove previous test enrollment
        // so the test can run again
        enrollmentRepository
                .findByStudent_IdAndCourse_Id(
                        student.getId(),
                        course.getId())
                .ifPresent(existing -> {
                    enrollmentRepository.delete(existing);
                    enrollmentRepository.flush();
                });

        // First enrollment should succeed
        Enrollment saved = enrollmentService.enroll(
                student.getId(),
                course.getId(),
                "A"
        );

        System.out.println("ENROLLMENT SAVED SUCCESSFULLY");
        System.out.println("Enrollment ID: " + saved.getId());
        System.out.println("Grade: " + saved.getGrade());

        // Second enrollment should fail
        try {

            enrollmentService.enroll(
                    student.getId(),
                    course.getId(),
                    "B"
            );

            System.out.println("ERROR: Duplicate was accepted!");

        } catch (DataIntegrityViolationException ex) {

            System.out.println(
                    "DUPLICATE ENROLLMENT REJECTED SUCCESSFULLY"
            );
        }
    }
}