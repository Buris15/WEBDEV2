package com.example.webdev2.service;

import com.example.webdev2.entity.*;
import com.example.webdev2.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CascadeDemoService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CascadeDemoService(
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository) {

        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public void runCascadeDemo() {

        // =====================================
        // TEST 1: CASCADE PERSIST
        // =====================================

        System.out.println("\n===== CASCADE PERSIST TEST =====");

        Department department = departmentRepository
                .findByName("Week9 Cascade Demo Department")
                .orElseGet(() -> {
                    Department newDepartment = new Department();
                    newDepartment.setName(
                            "Week9 Cascade Demo Department");
                    return newDepartment;
                });

        Employee employee = new Employee();
        employee.setName("Temporary Employee");
        employee.setPosition("Test Employee");

        // Add employee to department
        department.addEmployee(employee);

        // Saves Department and Employee together
        departmentRepository.saveAndFlush(department);

        Long employeeId = employee.getId();

        System.out.println(
                "Department saved: " + department.getName());

        System.out.println(
                "Employee saved through cascade: "
                + employeeRepository.existsById(employeeId));

        // =====================================
        // TEST 2: ORPHAN REMOVAL
        // =====================================

        System.out.println("\n===== ORPHAN REMOVAL TEST =====");

        // Remove employee from department
        department.removeEmployee(employee);

        departmentRepository.saveAndFlush(department);

        boolean employeeStillExists =
                employeeRepository.existsById(employeeId);

        System.out.println(
                "Employee exists after removal: "
                + employeeStillExists);

        System.out.println(
                "ORPHAN REMOVAL SUCCESSFUL: "
                + !employeeStillExists);

        // =====================================
        // TEST 3: SHARED COURSE PROTECTION
        // =====================================

        System.out.println("\n===== SHARED COURSE TEST =====");

        // Create or retrieve shared course
        Course course = courseRepository
                .findByCourseCode("W9-SHARED-COURSE")
                .orElseGet(() -> {

                    Course newCourse = new Course();
                    newCourse.setCourseCode("W9-SHARED-COURSE");
                    newCourse.setTitle("Shared Java Course");

                    return courseRepository.saveAndFlush(newCourse);
                });

        // Create or retrieve Student A
        Student studentA = studentRepository
                .findByStudentNumber("W9-SHARED-A")
                .orElseGet(() -> {

                    Student newStudent = new Student();
                    newStudent.setStudentNumber("W9-SHARED-A");
                    newStudent.setName("Student A");

                    return studentRepository.saveAndFlush(newStudent);
                });

        // Create or retrieve Student B
        Student studentB = studentRepository
                .findByStudentNumber("W9-SHARED-B")
                .orElseGet(() -> {

                    Student newStudent = new Student();
                    newStudent.setStudentNumber("W9-SHARED-B");
                    newStudent.setName("Student B");

                    return studentRepository.saveAndFlush(newStudent);
                });

        // Ensure both students have the same course
        createEnrollmentIfMissing(studentA, course, "A");
        createEnrollmentIfMissing(studentB, course, "B");

        enrollmentRepository.flush();

        Long courseId = course.getId();
        Long studentBId = studentB.getId();

        System.out.println("Student A enrolled.");
        System.out.println("Student B enrolled.");
        System.out.println("Both students share: " + course.getTitle());

        // =====================================
        // TEST 4: DELETE STUDENT A SAFELY
        // =====================================

        System.out.println("\n===== SAFE STUDENT DELETE TEST =====");

        // Explicitly delete Student A's enrollment records.
        // Do not cascade REMOVE to the shared Course.
        List<Enrollment> studentAEnrollments =
                enrollmentRepository.findByStudent_Id(
                        studentA.getId());

        for (Enrollment enrollment : studentAEnrollments) {
            studentA.removeEnrollment(enrollment);
            course.removeEnrollment(enrollment);
        }

        enrollmentRepository.deleteAll(studentAEnrollments);
        enrollmentRepository.flush();

        // Delete Student A
        studentRepository.delete(studentA);
        studentRepository.flush();

        // Verify Course and Student B's enrollment survive
        boolean courseSurvives =
                courseRepository.existsById(courseId);

        boolean studentBStillEnrolled =
                enrollmentRepository
                        .findByStudent_IdAndCourse_Id(
                                studentBId,
                                courseId)
                        .isPresent();

        System.out.println(
                "Student A deleted successfully.");

        System.out.println(
                "Shared course still exists: "
                + courseSurvives);

        System.out.println(
                "Student B still enrolled: "
                + studentBStillEnrolled);
    }

    private void createEnrollmentIfMissing(
            Student student,
            Course course,
            String grade) {

        boolean alreadyEnrolled = enrollmentRepository
                .findByStudent_IdAndCourse_Id(
                        student.getId(),
                        course.getId())
                .isPresent();

        if (!alreadyEnrolled) {

            Enrollment enrollment = new Enrollment();

            enrollment.setEnrollmentDate(LocalDate.now());
            enrollment.setGrade(grade);

            student.addEnrollment(enrollment);
            course.addEnrollment(enrollment);

            enrollmentRepository.saveAndFlush(enrollment);
        }
    }
}