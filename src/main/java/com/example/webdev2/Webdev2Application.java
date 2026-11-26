package com.example.webdev2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Webdev2Application {
    public static void main(String[] args) {
        if (args.length == 1 && "--hash-password".equals(args[0])) {
            com.example.webdev2.security.PasswordHashTool.runInteractive();
            return;
        }
        SpringApplication.run(Webdev2Application.class, args);
    }
}
