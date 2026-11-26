package com.example.webdev2.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.Console;
import java.util.Arrays;

/** One-time local helper. It prints a BCrypt hash and does not write or log the password. */
public final class PasswordHashTool {
    private PasswordHashTool() { }

    public static void runInteractive() {
        Console console = System.console();
        if (console == null) {
            System.err.println("No interactive console was detected. Run this command in Windows Terminal or CMD.");
            throw new IllegalStateException("A console is required so the password is not echoed.");
        }

        char[] password = console.readPassword("Enter a new local test-account password (12-72 characters): ");
        if (password == null || password.length < 12 || password.length > 72) {
            if (password != null) Arrays.fill(password, '\0');
            throw new IllegalArgumentException("Use a password between 12 and 72 characters.");
        }

        try {
            String encoded = new BCryptPasswordEncoder(12).encode(new String(password));
            System.out.println("BCrypt hash (paste into your local, untracked seed SQL file):");
            System.out.println(encoded);
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
