package com.example.webdev2;

import com.example.webdev2.service.CapstoneSmokeTestService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class CapstoneSmokeTestRunner implements CommandLineRunner {

    private final CapstoneSmokeTestService smokeTestService;

    public CapstoneSmokeTestRunner(
            CapstoneSmokeTestService smokeTestService) {

        this.smokeTestService = smokeTestService;
    }

    @Override
    public void run(String... args) {

        smokeTestService.runSmokeTest();
    }
}