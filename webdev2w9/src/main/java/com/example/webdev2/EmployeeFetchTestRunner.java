package com.example.webdev2;

import com.example.webdev2.entity.Employee;
import com.example.webdev2.repository.EmployeeRepository;
import com.example.webdev2.service.EmployeeFetchService;

import org.hibernate.LazyInitializationException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class EmployeeFetchTestRunner implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final EmployeeFetchService employeeFetchService;

    public EmployeeFetchTestRunner(
            EmployeeRepository employeeRepository,
            EmployeeFetchService employeeFetchService) {

        this.employeeRepository = employeeRepository;
        this.employeeFetchService = employeeFetchService;
    }

    @Override
    public void run(String... args) {

        // Find an existing employee
        Employee existingEmployee = employeeRepository.findAll()
                .stream()
                .findFirst()
                .orElse(null);

        if (existingEmployee == null) {
            System.out.println("No employee records found.");
            return;
        }

        Long employeeId = existingEmployee.getId();

        System.out.println("\n===== FETCH STRATEGY TEST =====");

        // METHOD 1: LAZY WITHOUT TRANSACTION
        System.out.println("\nTEST 1: LAZY WITHOUT TRANSACTION");

        try {

            Employee employee =
                    employeeFetchService
                            .getEmployeeWithoutTransaction(employeeId);

            System.out.println(
                    employee.getDepartment().getName());

        } catch (LazyInitializationException ex) {

            System.out.println(
                    "LAZY INITIALIZATION EXCEPTION REPRODUCED!");

            System.out.println(ex.getClass().getSimpleName());
        }

        // METHOD 2: @TRANSACTIONAL
        System.out.println("\nTEST 2: USING @TRANSACTIONAL");

        String departmentName =
                employeeFetchService
                        .getDepartmentNameTransactional(employeeId);

        System.out.println(
                "Department: " + departmentName);

        System.out.println(
                "TRANSACTIONAL FIX SUCCESSFUL!");

        // METHOD 3: JOIN FETCH
        System.out.println("\nTEST 3: USING JOIN FETCH");

        Employee fetchedEmployee =
                employeeFetchService
                        .getEmployeeWithFetchJoin(employeeId);

        System.out.println(
                "Employee: " + fetchedEmployee.getName());

        System.out.println(
                "Department: "
                + fetchedEmployee.getDepartment().getName());

        System.out.println(
                "JOIN FETCH FIX SUCCESSFUL!");
    }
}