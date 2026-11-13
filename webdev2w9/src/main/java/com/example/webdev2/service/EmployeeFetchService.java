package com.example.webdev2.service;

import com.example.webdev2.entity.Employee;
import com.example.webdev2.repository.EmployeeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeFetchService {

    private final EmployeeRepository employeeRepository;

    public EmployeeFetchService(
            EmployeeRepository employeeRepository) {

        this.employeeRepository = employeeRepository;
    }

    // METHOD 1: No transaction
    public Employee getEmployeeWithoutTransaction(Long id) {

        return employeeRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Employee not found"));
    }

    // METHOD 2: Fix using @Transactional
    @Transactional(readOnly = true)
    public String getDepartmentNameTransactional(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Employee not found"));

        return employee.getDepartment().getName();
    }

    // METHOD 3: Fix using JOIN FETCH
    public Employee getEmployeeWithFetchJoin(Long id) {

        return employeeRepository.findByIdWithDepartment(id)
                .orElseThrow(() ->
                    new RuntimeException("Employee not found"));
    }
}