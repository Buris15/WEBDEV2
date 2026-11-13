package com.example.webdev2;

import com.example.webdev2.entity.*;
import com.example.webdev2.repository.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
@Order(1)
@Component
public class RelationshipSmokeTestRunner
        implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public RelationshipSmokeTestRunner(
            UserRepository userRepository,
            DepartmentRepository departmentRepository) {

        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // ONE-TO-ONE TEST
        if (userRepository.findByUsername("week9_user").isEmpty()) {

            User user = new User();

            user.setUsername("week9_user");
            user.setEmail("week9@example.com");
            user.setCreatedAt(LocalDateTime.now());

            UserProfile profile = new UserProfile();

            profile.setPhoneNumber("09123456789");
            profile.setAddress("La Union, Philippines");

            user.setProfile(profile);

            userRepository.save(user);

            System.out.println("ONE-TO-ONE SAVED SUCCESSFULLY");
        }

        // ONE-TO-MANY TEST
        if (!departmentRepository.existsByName("IT Department")) {

            Department department = new Department();

            department.setName("IT Department");

            Employee employee1 = new Employee();
            employee1.setName("John");
            employee1.setPosition("Developer");

            Employee employee2 = new Employee();
            employee2.setName("Mark");
            employee2.setPosition("System Administrator");

            // Add employees to department
            // This updates both sides of the relationship
            department.addEmployee(employee1);
            department.addEmployee(employee2);

            // Saves the department and its employees
            departmentRepository.save(department);

            System.out.println("ONE-TO-MANY SAVED SUCCESSFULLY");
        }

        // READ BACK THE DEPARTMENT AND EMPLOYEES
        System.out.println("\n===== DEPARTMENT RECORDS =====");

        departmentRepository.findAll().forEach(department -> {

            System.out.println("Department: " + department.getName());

            department.getEmployees().forEach(employee -> {
                System.out.println("Employee: " + employee.getName());
                System.out.println("Position: " + employee.getPosition());
            });

            System.out.println("-------------------------");
        });

        // READ BACK THE USER AND PROFILE
        userRepository.findByUsername("week9_user").ifPresent(user -> {

            System.out.println("\n===== USER PROFILE =====");
            System.out.println("Username: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            System.out.println(
                    "Phone: " + user.getProfile().getPhoneNumber());
            System.out.println(
                    "Address: " + user.getProfile().getAddress());
        });
    }
}