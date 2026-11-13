package com.example.webdev2.repository;

import com.example.webdev2.entity.Employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    // Fetch Employee together with its Department
    @Query("""
        SELECT e
        FROM Employee e
        JOIN FETCH e.department
        WHERE e.id = :id
    """)
    Optional<Employee> findByIdWithDepartment(
            @Param("id") Long id);
}