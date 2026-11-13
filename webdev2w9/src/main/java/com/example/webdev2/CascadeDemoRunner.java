package com.example.webdev2;

import com.example.webdev2.service.CascadeDemoService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class CascadeDemoRunner implements CommandLineRunner {

    private final CascadeDemoService cascadeDemoService;

    public CascadeDemoRunner(
            CascadeDemoService cascadeDemoService) {

        this.cascadeDemoService = cascadeDemoService;
    }

    @Override
    public void run(String... args) {

        cascadeDemoService.runCascadeDemo();
    }
}